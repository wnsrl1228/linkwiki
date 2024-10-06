package com.linkwiki.link.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor()
@Getter
public class LinkModifyRequest {

    @NotNull(message = "태그를 한 개 이상 선택해주세요.")
    @Size(min = 1, max = 5, message = "태그는 최대 5개까지 선택할 수 있습니다.")
    private List<String> tags;

    public LinkModifyRequest(List<String> tags) {
        this.tags = tags;
    }
}
