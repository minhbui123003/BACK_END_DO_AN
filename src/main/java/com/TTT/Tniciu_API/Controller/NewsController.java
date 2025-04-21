package com.TTT.Tniciu_API.Controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.TTT.Tniciu_API.Model.New;
import com.TTT.Tniciu_API.Service.NewsService;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping
    public List<New> getAllNews() {
        return newsService.getAllNews();
    }

    @GetMapping("/{id}")
    public ResponseEntity<New> getNewsById(@PathVariable Long id) {
        New news = newsService.getNewsById(id);
        return news != null ? new ResponseEntity<>(news, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public New createNews(@RequestBody New news) {
        return newsService.saveNews(news);
    }

    @PutMapping("/{id}")
    public ResponseEntity<New> updateNews(@PathVariable Long id, @RequestBody New newsDetails) {
        New updatedNews = newsService.updateNews(id, newsDetails);
        return updatedNews != null ? new ResponseEntity<>(updatedNews, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
