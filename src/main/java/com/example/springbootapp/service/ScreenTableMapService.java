package com.example.springbootapp.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springbootapp.config.ScreenTableResolver;
import com.example.springbootapp.domain.ScreenList;
import com.example.springbootapp.domain.ScreenTableMap;
import com.example.springbootapp.mapper.ScreenTableMapMapper;

@Service
public class ScreenTableMapService {

	private final ScreenTableMapMapper screenTableMapMapper;

	public ScreenTableMapService(ScreenTableMapMapper screenTableMapMapper) {
		this.screenTableMapMapper = screenTableMapMapper;
	}

	public Map<String, ScreenTableMap> findAllByUri() {
		return screenTableMapMapper.findAll().stream()
				.collect(Collectors.toMap(ScreenTableMap::getUriPath, m -> m, (a, b) -> a, LinkedHashMap::new));
	}

	public ScreenTableMap findByUriPath(String uriPath) {
		return screenTableMapMapper.findByUriPath(uriPath);
	}

	@Transactional
	public void syncFromScreens(List<ScreenList> screens, String actor) {
		LocalDateTime now = LocalDateTime.now();
		for (ScreenList screen : screens) {
			ScreenTableResolver.Mapping mapping = ScreenTableResolver.resolve(screen.getUriPath());
			ScreenTableMap row = new ScreenTableMap();
			row.setUriPath(screen.getUriPath());
			row.setScreenId(screen.getScreenId());
			row.setPrimaryTable(mapping.primaryTable());
			row.setRelatedTables(mapping.relatedTables());
			row.setDataType(String.valueOf(mapping.dataType()));
			row.setTableDesc(mapping.tableDesc());
			row.setRegId(actor);
			row.setRegDt(now);
			row.setUpdateId(actor);
			row.setUpdateDt(now);
			screenTableMapMapper.upsert(row);
		}
	}
}
