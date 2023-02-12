package com.project.myacademy.domain.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateLectureRequest {
    @NotBlank(message = "강좌 이름은 필수 입력 항목입니다.")
    private String lectureName;

    @NotNull(message = "강좌 가격은 필수 입력 항목입니다.")
    @Min(value = 1000,message = "강의 가격이 1000원 미만일 수 없습니다.")
    private Integer lecturePrice;

    @NotNull(message = "최소 수강 정원은 필수 입력 항목입니다.")
    @Min(value = 1,message = "최소 수강인원은 1명 미만일 수 없습니다.")
    private Integer minimumCapacity;

    @NotNull(message = "최대 수강 정원은 필수 입력 항목입니다.")
    @Min(value = 1,message = "최대 수강인원은 1명 미만일 수 없습니다.")
    private Integer maximumCapacity;

    @NotBlank(message = "강좌 요일은 필수 입력 항목입니다.")
    private String lectureDay;
    @NotBlank(message = "강좌 시간은 필수 입력 항목입니다.")
    private String lectureTime;
    @NotNull(message = "강좌 시작일은 필수 입력 항목입니다.")
    @Future(message = "강좌 시작일이 과거일 수 없습니다.")
    private LocalDate startDate;
    @NotNull(message = "강좌 종료일은 필수 입력 항목입니다.")
    private LocalDate finishDate;
}
