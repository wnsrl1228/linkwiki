package com.linkwiki.link.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor()
@Getter
@Setter // @ModelAttribute는 setter 사용
public class LinkSearchRequest {

    @NotNull(message = "메인 카테고리를 선택해주세요.")
    private Long categoryTagId;

    @NotNull(message = "태그를 한 개 이상 선택해주세요.")
    @Size(min = 1, max = 12, message = "태그는 최대 12개까지 선택할 수 있습니다.")
    private List<Long> tagId;

    public LinkSearchRequest(Long categoryTagId, List<Long> tagId) {
        this.categoryTagId = categoryTagId;
        this.tagId = tagId;
    }
}
