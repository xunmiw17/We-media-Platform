package com.xunmiw.article.controller;

import com.xunmiw.pojo.Article;
import com.xunmiw.pojo.Spouse;
import com.xunmiw.pojo.Stu;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

@Controller
@RequestMapping("free")
public class FreemarkerController {

    @Value("${freemarker.html.target}")
    private String htmlTarget;

    @GetMapping("createHTML")
    @ResponseBody
    public String createHTML(Model model) throws Exception {
        // 0. 配置freemarker基本环境
        Configuration config = new Configuration(Configuration.getVersion());
        // 声明freemarker模板所需要加载的目录的位置
        String classPath = this.getClass().getResource("/").getPath();
        config.setDirectoryForTemplateLoading(new File(classPath + "templates"));

        // 1. 获得现有的模板ftl文件
        Template template = config.getTemplate("stu.ftl", "utf-8");

        // 2. 获得动态数据
        String stranger = "frank";
        model.addAttribute("name", stranger);
        model = makeModel(model);

        // 3. 融合动态数据和ftl，生成html
        File temp = new File(htmlTarget);
        if (!temp.exists()) {
            temp.mkdirs();
        }

        Writer out = new FileWriter(htmlTarget + File.separator + "10010" + ".html");
        template.process(model, out);
        out.close();

        return "ok";
    }

    @GetMapping("hello")
    public String hello(Model model) {

        String stranger = "frank";
        model.addAttribute("name", stranger);

        makeModel(model);

        return "stu";
    }

    private Model makeModel(Model model) {
        Stu stu = new Stu();
        stu.setUid("10010");
        stu.setUsername("imooc");
        stu.setAge(18);
        stu.setAmount(88.86f);
        stu.setHaveChild(true);
        stu.setBirthday(new Date());

        Spouse spouse = new Spouse();
        spouse.setUsername("lucy");
        spouse.setAge(25);

        stu.setSpouse(spouse);
        stu.setArticleList(getArticles());
        stu.setParents(getParents());

        model.addAttribute("stu", stu);
        return model;
    }

    private List<Article> getArticles() {
        Article article1 = new Article();
        article1.setId("1001");
        article1.setTitle("Good article");

        Article article2 = new Article();
        article2.setId("1002");
        article2.setTitle("Good e");

        Article article3 = new Article();
        article3.setId("1003");
        article3.setTitle("Good artile");

        List<Article> list = new ArrayList<>();
        list.add(article1);
        list.add(article2);
        list.add(article3);
        return list;
    }

    private Map<String, String> getParents() {
        Map<String, String> parents = new HashMap<>();
        parents.put("father", "frank");
        parents.put("mother", "me");
        return parents;
    }
}
