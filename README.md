IK Analysis for Elasticsearch
=============================

The IK Analysis plugin integrates Lucene IK analyzer (http://code.google.com/p/ik-analyzer/) into elasticsearch, support customized dictionary.

Analyzer: `ik_smart` , `ik_max_word` , Tokenizer: `ik_smart` , `ik_max_word`

Versions
--------

IK version | ES version
-----------|-----------
master | 7.x -> master
6.x| 6.x
5.x| 5.x
1.10.6 | 2.4.6
1.9.5 | 2.3.5
1.8.1 | 2.2.1
1.7.0 | 2.1.1
1.5.0 | 2.0.0
1.2.6 | 1.0.0
1.2.5 | 0.90.x
1.1.3 | 0.20.x
1.0.0 | 0.16.2 -> 0.19.0

Install
-------

1.download or compile

* optional 1 - download pre-build package from here: https://github.com/medcl/elasticsearch-analysis-ik/releases

    create plugin folder `cd your-es-root/plugins/ && mkdir ik`
    
    unzip plugin to folder `your-es-root/plugins/ik`

* optional 2 - use elasticsearch-plugin to install ( supported from version v5.5.1 ):

    ```
    ./bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v6.3.0/elasticsearch-analysis-ik-6.3.0.zip
    ```

   NOTE: replace `6.3.0` to your own elasticsearch version

2.restart elasticsearch



#### Quick Example

1.create a index

```bash
curl -XPUT http://localhost:9200/index
```

2.create a mapping

```bash
curl -XPOST http://localhost:9200/index/_mapping -H 'Content-Type:application/json' -d'
{
        "properties": {
            "content": {
                "type": "text",
                "analyzer": "ik_max_word",
                "search_analyzer": "ik_smart"
            }
        }

}'
```

3.index some docs

```bash
curl -XPOST http://localhost:9200/index/_create/1 -H 'Content-Type:application/json' -d'
{"content":"美國留給伊拉克的是個爛攤子嗎"}
'
```

```bash
curl -XPOST http://localhost:9200/index/_create/2 -H 'Content-Type:application/json' -d'
{"content":"公安部：各地校車將享最高路權"}
'
```

```bash
curl -XPOST http://localhost:9200/index/_create/3 -H 'Content-Type:application/json' -d'
{"content":"中韓漁警衝突調查：韓警平均每天扣1艘中國漁船"}
'
```

```bash
curl -XPOST http://localhost:9200/index/_create/4 -H 'Content-Type:application/json' -d'
{"content":"中國駐洛杉磯領事館遭亞裔男子槍擊 嫌犯已自首"}
'
```

4.query with highlighting

```bash
curl -XPOST http://localhost:9200/index/_search  -H 'Content-Type:application/json' -d'
{
    "query" : { "match" : { "content" : "中國" }},
    "highlight" : {
        "pre_tags" : ["<tag1>", "<tag2>"],
        "post_tags" : ["</tag1>", "</tag2>"],
        "fields" : {
            "content" : {}
        }
    }
}
'
```

Result

```json
{
    "took": 14,
    "timed_out": false,
    "_shards": {
        "total": 5,
        "successful": 5,
        "failed": 0
    },
    "hits": {
        "total": 2,
        "max_score": 2,
        "hits": [
            {
                "_index": "index",
                "_type": "fulltext",
                "_id": "4",
                "_score": 2,
                "_source": {
                    "content": "中國駐洛杉磯領事館遭亞裔男子槍擊 嫌犯已自首"
                },
                "highlight": {
                    "content": [
                        "<tag1>中國</tag1>駐洛杉磯領事館遭亞裔男子槍擊 嫌犯已自首 "
                    ]
                }
            },
            {
                "_index": "index",
                "_type": "fulltext",
                "_id": "3",
                "_score": 2,
                "_source": {
                    "content": "中韓漁警衝突調查：韓警平均每天扣1艘中國漁船"
                },
                "highlight": {
                    "content": [
                        "均每天扣1艘<tag1>中國</tag1>漁船 "
                    ]
                }
            }
        ]
    }
}
```

### Dictionary Configuration

`IKAnalyzer.cfg.xml` can be located at `{conf}/analysis-ik/config/IKAnalyzer.cfg.xml`
or `{plugins}/elasticsearch-analysis-ik-*/config/IKAnalyzer.cfg.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
	<comment>IK Analyzer 擴充套件配置</comment>
	<!--使用者可以在這裡配置自己的擴充套件字典 -->
	<entry key="ext_dict">custom/mydict.dic;custom/single_word_low_freq.dic</entry>
	 <!--使用者可以在這裡配置自己的擴充套件停止詞字典-->
	<entry key="ext_stopwords">custom/ext_stopword.dic</entry>
 	<!--使用者可以在這裡配置遠端擴充套件字典 -->
	<entry key="remote_ext_dict">location</entry>
 	<!--使用者可以在這裡配置遠端擴充套件停止詞字典-->
	<entry key="remote_ext_stopwords">http://xxx.com/xxx.dic</entry>
</properties>
```

### 熱更新 IK 分詞使用方法

目前該外掛支援熱更新 IK 分詞，透過上文在 IK 配置檔案中提到的如下配置

```xml
 	<!--使用者可以在這裡配置遠端擴充套件字典 -->
	<entry key="remote_ext_dict">location</entry>
 	<!--使用者可以在這裡配置遠端擴充套件停止詞字典-->
	<entry key="remote_ext_stopwords">location</entry>
```

其中 `location` 是指一個 url，比如 `http://yoursite.com/getCustomDict`，該請求只需滿足以下兩點即可完成分詞熱更新。

1. 該 http 請求需要返回兩個頭部(header)，一個是 `Last-Modified`，一個是 `ETag`，這兩者都是字串型別，只要有一個發生變化，該外掛就會去抓取新的分詞進而更新詞庫。

2. 該 http 請求返回的內容格式是一行一個分詞，換行符用 `\n` 即可。

滿足上面兩點要求就可以實現熱更新分詞了，不需要重啟 ES 例項。

可以將需自動更新的熱詞放在一個 UTF-8 編碼的 .txt 檔案裡，放在 nginx 或其他簡易 http server 下，當 .txt 檔案修改時，http server 會在客戶端請求該檔案時自動返回相應的 Last-Modified 和 ETag。可以另外做一個工具來從業務系統提取相關詞彙，並更新這個 .txt 檔案。

have fun.

常見問題
-------

1.自定義詞典為什麼沒有生效？

請確保你的擴充套件詞典的文字格式為 UTF8 編碼

2.如何手動安裝？


```bash
git clone https://github.com/medcl/elasticsearch-analysis-ik
cd elasticsearch-analysis-ik
git checkout tags/{version}
mvn clean
mvn compile
mvn package
```

複製和解壓release下的檔案: #{project_path}/elasticsearch-analysis-ik/target/releases/elasticsearch-analysis-ik-*.zip 到你的 elasticsearch 外掛目錄, 如: plugins/ik
重啟elasticsearch

3.分詞測試失敗
請在某個索引下呼叫analyze介面測試,而不是直接呼叫analyze介面
如:
```bash
curl -XGET "http://localhost:9200/your_index/_analyze" -H 'Content-Type: application/json' -d'
{
   "text":"中華人民共和國MN","tokenizer": "my_ik"
}'
```


4. ik_max_word 和 ik_smart 什麼區別?


ik_max_word: 會將文字做最細粒度的拆分，比如會將“中華人民共和國國歌”拆分為“中華人民共和國,中華人民,中華,華人,人民共和國,人民,人,民,共和國,共和,和,國國,國歌”，會窮盡各種可能的組合，適合 Term Query；

ik_smart: 會做最粗粒度的拆分，比如會將“中華人民共和國國歌”拆分為“中華人民共和國,國歌”，適合 Phrase 查詢。

Changes
------
*自 v5.0.0 起*

- 移除名為 `ik` 的analyzer和tokenizer,請分別使用 `ik_smart` 和 `ik_max_word`


Thanks
------
YourKit supports IK Analysis for ElasticSearch project with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
<a href="http://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a> and
<a href="http://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>.
