package com.school.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentDirectoryDTO {
    private long parentId;
    private String firstName;
    private String lastName;
    private String phone;
    private String occupation;
}
