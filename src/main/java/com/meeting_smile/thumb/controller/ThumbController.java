package com.meeting_smile.thumb.controller;


import com.meeting_smile.thumb.common.BaseResponse;
import com.meeting_smile.thumb.common.ResultUtils;
import com.meeting_smile.thumb.model.dto.thumb.DoThumbRequest;
import com.meeting_smile.thumb.service.ThumbService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author meeting_smile
 * @since 2025-05-13
 */
@RestController
@RequestMapping("thumb")
public class ThumbController {
    @Resource
    private ThumbService thumbService;

    @PostMapping("/do")
    public BaseResponse<Boolean> doThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        Boolean success = thumbService.doThumb(doThumbRequest, request);
        return ResultUtils.success(success);
    }

    @PostMapping("/undo")
    public BaseResponse<Boolean> undoThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        Boolean success = thumbService.undoThumb(doThumbRequest, request);
        return ResultUtils.success(success);
    }
}

