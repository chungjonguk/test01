import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import {
  Document,
  Packer,
  Paragraph,
  TextRun,
  HeadingLevel,
  ImageRun,
  PageBreak,
  AlignmentType,
} from "docx";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const sections = JSON.parse(
  fs.readFileSync(path.join(__dirname, "portfolio-sections.json"), "utf8")
);

function bodyParagraphs(lines) {
  return lines.map(
    (t) =>
      new Paragraph({
        spacing: { after: 120 },
        children: [new TextRun({ text: t, size: 22 })],
      })
  );
}

function imageParagraph(relPath) {
  const imgPath = path.join(__dirname, relPath.replace(/\//g, path.sep));
  if (!fs.existsSync(imgPath)) return [];
  const data = fs.readFileSync(imgPath);
  return [
    new Paragraph({
      spacing: { before: 200, after: 200 },
      children: [
        new ImageRun({
          data,
          transformation: { width: 520, height: 320 },
        }),
      ],
    }),
  ];
}

const children = [
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 200 },
    children: [
      new TextRun({ text: "Spring Boot App (Falcon UI)", bold: true, size: 44 }),
    ],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 120 },
    children: [
      new TextRun({
        text: "포트폴리오 — 개발환경 및 화면 설명",
        size: 28,
      }),
    ],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [
      new TextRun({
        text: new Date().toISOString().slice(0, 10),
        size: 22,
        color: "666666",
      }),
    ],
  }),
  new Paragraph({ children: [new PageBreak()] }),
];

for (const sec of sections) {
  children.push(
    new Paragraph({
      heading: HeadingLevel.HEADING_1,
      spacing: { before: 240, after: 120 },
      children: [new TextRun({ text: sec.title, bold: true, size: 32 })],
    })
  );
  children.push(...bodyParagraphs(sec.body));
  if (sec.image) {
    children.push(...imageParagraph(sec.image));
  }
}

const doc = new Document({ sections: [{ properties: {}, children }] });
const outPath = path.join(__dirname, "portfolio-spring-boot-app.docx");
const buffer = await Packer.toBuffer(doc);
fs.writeFileSync(outPath, buffer);
console.log("Created:", outPath, buffer.length, "bytes");
