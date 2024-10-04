package com.linkwiki.link.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@NoArgsConstructor()
@Getter
public class LinkCreateRequest {

    @Length(max = 100, message = "최대 100자까지 입력할 수 있습니다.")
    @NotBlank(message = "링크 설명을 입력해주세요.")
    private String description;

    @Length(max = 100, message = "최대 100자까지 입력할 수 있습니다.")
    @NotBlank(message = "링크 설명을 입력해주세요.")
    private String url;

    @NotNull(message = "메인 카테고리를 선택해주세요.")
    private Long categoryTagId;

    @NotNull(message = "태그를 한 개 이상 선택해주세요.")
    @Size(min = 1, max = 5, message = "태그는 최대 5개까지 선택할 수 있습니다.")
    private List<String> tags;

    public LinkCreateRequest(String description, String url, Long categoryTagId, List<String> tags) {
        this.description = description;
        this.url = url;
        this.categoryTagId = categoryTagId;
        this.tags = tags;
    }
}
