package com.xunmiw.pojo.bo;

import javax.validation.constraints.NotBlank;

public class CategoryBO {
    @NotBlank
    private String name;
    private String tagColor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagColor() {
        return tagColor;
    }

    public void setTagColor(String tagColor) {
        this.tagColor = tagColor;
    }
}