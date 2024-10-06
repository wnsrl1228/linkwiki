package com.linkwiki.link.dto.request;

import com.linkwiki.link.domain.LinkState;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor()
@Getter
public class LinkReviewRequest {

    @NotNull(message = "검토 결과값은 필수입니다.")
    private Boolean result;

    private String reason;

    public LinkReviewRequest(Boolean result, String reason) {
        this.result = result;
        this.reason = reason;
    }
}
