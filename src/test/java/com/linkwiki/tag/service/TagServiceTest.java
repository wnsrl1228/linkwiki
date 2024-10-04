package com.linkwiki.tag.service;

import com.linkwiki.tag.domain.Tag;
import com.linkwiki.tag.dto.TagElement;
import com.linkwiki.tag.dto.response.TagsResponse;
import com.linkwiki.tag.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql({"/h2-truncate.sql"})
class TagServiceTest {

    @Autowired
    private TagService tagService;
    @Autowired
    private TagRepository tagRepository;

    private static final String TAG_1 = "tag1";
    private static final String TAG_2 = "tag2";
    private static final String TAG_3 = "tag3";

    @Test
    @DisplayName("태그 생성에 성공한다.")
    void createTags_success_1() {
        // given
        List<String> tags = List.of(TAG_1, TAG_2, TAG_3);
        // when
        tagService.createTags(tags);
        // then
        List<Tag> newTags = tagRepository.findAll();
        assertThat(newTags.size()).isEqualTo(tags.size());
        assertThat(newTags.get(0).getName()).isEqualTo(tags.get(0));
        assertThat(newTags.get(1).getName()).isEqualTo(tags.get(1));
        assertThat(newTags.get(2).getName()).isEqualTo(tags.get(2));
    }

    @Test
    @DisplayName("새로운 태그만 db에 저장된다.")
    void createTags_success_2() {
        // given
        tagRepository.save(new Tag(TAG_2));
        List<String> tags = List.of(TAG_1, TAG_2, TAG_3);
        // when
        tagService.createTags(tags);
        // then
        List<Tag> newTags = tagRepository.findAll();
        assertThat(newTags.size()).isEqualTo(tags.size());
        assertThat(newTags.get(0).getName()).isEqualTo(TAG_2);
        assertThat(newTags.get(1).getName()).isEqualTo(tags.get(0));
        assertThat(newTags.get(2).getName()).isEqualTo(tags.get(2));
    }

    @Test
    @DisplayName("태그 자동완성 조회에 성공한다.")
    void getTagsByKeyword_success_1() {
        // given
        tagRepository.save(new Tag(TAG_1));
        tagRepository.save(new Tag(TAG_2));
        tagRepository.save(new Tag(TAG_3));
        // when
        TagsResponse tag = tagService.getTagsByKeyword("tag");
        // then
        List<TagElement> tags = tag.getTags();
        assertThat(tag.getTags().size()).isEqualTo(3);
        assertThat(tags.get(0).getName()).isEqualTo(TAG_1);
        assertThat(tags.get(1).getName()).isEqualTo(TAG_2);
        assertThat(tags.get(2).getName()).isEqualTo(TAG_3);
    }
}