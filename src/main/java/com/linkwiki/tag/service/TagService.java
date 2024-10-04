package com.linkwiki.tag.service;

import com.linkwiki.tag.domain.Tag;
import com.linkwiki.tag.dto.response.TagsResponse;
import com.linkwiki.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    // 테이블에 존재하지 않은 tag만 추가
    public List<Tag> createTags(List<String> tags) {

        Map<String, Tag> existingTagsMap = tagRepository.findByNameIn(tags)
                .stream()
                .collect(Collectors.toMap(Tag::getName, tag -> tag));

        return tags.stream()
                .map(tag -> {
                    // 이미 존재하는 태그를 체크
                    if (existingTagsMap.containsKey(tag)) {
                        return existingTagsMap.get(tag);
                    } else {
                        // 존재하지 않는 경우 새로 태그를 생성하고 저장
                        return tagRepository.save(new Tag(tag));
                    }
                })
                .collect(Collectors.toList());
    }

    // keyword 를 포함한 태그 조회
    public TagsResponse getTagsByKeyword(String keyword) {
        // TODO : 추후 고도화 작업 진행할 예정
        List<Tag> tagsByKeyword = tagRepository.findTagsByKeyword(keyword, PageRequest.of(0, 10));
        return TagsResponse.of(tagsByKeyword);
    }
}
