package com.henh.testman.workspaces.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WorkspaceUpdateReq {

    private Long seq;

    private String title;

    private String url;

    private String description;

}