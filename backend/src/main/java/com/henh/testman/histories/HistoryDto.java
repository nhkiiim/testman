package com.henh.testman.histories;

import com.henh.testman.uri_info.UriInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HistoryDto {

    private final Long seq;

    private final UriInfo uriInfo;

    public HistoryDto(History history) {
        this.seq = history.getSeq();
        this.uriInfo = history.getUriInfo();
    }

}