/**
 * IK 中文分詞  版本 5.0.1
 * IK Analyzer release 5.0.1
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * 原始碼由林良益(linliangyi2005@gmail.com)提供
 * 版權宣告 2012，烏龍茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 */
package org.wltea.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.wltea.analyzer.cfg.Configuration;

/**
 * IK分詞器，Lucene Analyzer介面實現
 * 相容Lucene 4.0版本
 */
public final class IKAnalyzer extends Analyzer {

    private Configuration configuration;

    /**
     * IK分詞器Lucene  Analyzer介面實現類
     *
     * 預設細粒度切分演算法
     */
    public IKAnalyzer() {
    }

    /**
     * IK分詞器Lucene Analyzer介面實現類
     *
     * @param configuration IK配置
     */
    public IKAnalyzer(Configuration configuration) {
        super();
        this.configuration = configuration;
    }


    /**
     * 過載Analyzer介面，構造分片語件
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer _IKTokenizer = new IKTokenizer(configuration);
        return new TokenStreamComponents(_IKTokenizer);
    }

}
