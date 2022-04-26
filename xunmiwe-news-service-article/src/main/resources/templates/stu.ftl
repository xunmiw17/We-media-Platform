<html>
    <head>
        <title>Hello</title>
    </head>
    <body>
        <div>
            Hello ${name}
        </div>
    </body>

<br>

    <div>
        用户名uid: ${stu.uid}
        <br>
        用户名: ${stu.username}
        <br>
        用户年龄: ${stu.age}
        <br>
        用户生日: ${stu.birthday?string('yyyy-MM-dd HH:mm:ss')}
        <br>
        用户金额: ${stu.amount}
        <br>
        已育: ${stu.haveChild?string('yes', 'no')}
        <br>
        伴侣: ${stu.spouse.username}, ${stu.spouse.age}岁
    </div>

<br>

    <div>
        <#list stu.articleList as article>
            <div>
                <span>${article.id}</span>
                <span>${article.title}</span>
            </div>
        </#list>
    </div>

<br>

    <div>
        <#list stu.parents?keys as key>
            <div>
                ${stu.parents[key]}
            </div>
        </#list>
    </div>

<br>

    <div>
        <#if stu.uid == '10010'>
            用户id为10010
        </#if>
        <#if stu.username != 'imooc'>
            用户名不是imooc
        </#if>
        <#if (stu.age >= 18)>
            用户已成年
        </#if>
        <#if stu.haveChild>
            已育
        </#if>
        <#if !stu.haveChild>
            未育
        </#if>
        <#if stu.spouse??>
            有伴侣
        </#if>
        <#if !stu.spouse??>
            无伴侣
        </#if>
    </div>

</html>