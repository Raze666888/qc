package com.qc.controller;

import com.qc.dto.AnswerDTO;
import com.qc.service.AnswerService;
import com.qc.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/answer")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @PostMapping("/submit")
    public Result<Long> submit(@RequestBody AnswerDTO answerDTO) {
        Long id = answerService.submitAnswer(answerDTO);
        return Result.success(id);
    }

    @GetMapping("/list/{questionnaireId}")
    public Result<List<Map<String, Object>>> list(@PathVariable Long questionnaireId) {
        List<Map<String, Object>> result = answerService.getAnswerList(questionnaireId);
        return Result.success(result);
    }

    @GetMapping("/statistics/{questionnaireId}")
    public Result<Map<String, Object>> statistics(@PathVariable Long questionnaireId) {
        Map<String, Object> result = answerService.getStatistics(questionnaireId);
        return Result.success(result);
    }
}
