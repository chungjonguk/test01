@echo off
setlocal EnableExtensions
chcp 65001 >nul

set NAS_ROOT=D:\nas-storage\printmall\uploads

echo.
echo === NAS 저장 폴더 생성 (이미지·문서·영상·상품) ===
echo   %NAS_ROOT%
echo.

for %%D in (images documents videos products) do (
  if not exist "%NAS_ROOT%\%%D" (
    mkdir "%NAS_ROOT%\%%D"
    echo [생성] %NAS_ROOT%\%%D
  ) else (
    echo [확인] %NAS_ROOT%\%%D
  )
)

echo.
echo  images    — 일반 이미지 (jpg, png, gif, webp, svg...)
echo  documents — 문서 (pdf, docx, xlsx, hwp, zip...)
echo  videos    — 영상 (mp4, webm, mov...)
echo  products  — 상품 이미지
echo.
echo API: POST /api/storage/upload?type=image^|document^|video^|product
echo 관리: http://localhost:8081/admin/media-storage
echo.
pause
endlocal
