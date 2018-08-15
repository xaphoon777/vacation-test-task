package com.example.vacation.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class Vacation {
    @Id
    @GeneratedValue
    @ApiModelProperty(hidden = true)
    private Long id;

    @ApiModelProperty(required = true, example = "10.02.2018")
    @JsonFormat(pattern = "dd.MM.yyyy")
    private Date start;

    @ApiModelProperty(required = true, example = "13.02.2018")
    @JsonFormat(pattern = "dd.MM.yyyy")
    private Date end;

    private String name;
    private VacationStatus status;

}
