/**
 * IK 中文分詞  版本 5.0
 * IK Analyzer release 5.0
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
package org.wltea.analyzer.dic;

/**
 * 表示一次詞典匹配的命中
 */
public class Hit {
    //Hit不匹配
    private static final int UNMATCH = 0x00000000;
    //Hit完全匹配
    private static final int MATCH = 0x00000001;
    //Hit字首匹配
    private static final int PREFIX = 0x00000010;


    //該HIT當前狀態，預設未匹配
    private int hitState = UNMATCH;

    //記錄詞典匹配過程中，當前匹配到的詞典分支節點
    private DictSegment matchedDictSegment;
    /*
     * 詞段開始位置
     */
    private int begin;
    /*
     * 詞段的結束位置
     */
    private int end;


    /**
     * 判斷是否完全匹配
     */
    public boolean isMatch() {
        return (this.hitState & MATCH) > 0;
    }

    /**
     *
     */
    public void setMatch() {
        this.hitState = this.hitState | MATCH;
    }

    /**
     * 判斷是否是詞的字首
     */
    public boolean isPrefix() {
        return (this.hitState & PREFIX) > 0;
    }

    /**
     *
     */
    public void setPrefix() {
        this.hitState = this.hitState | PREFIX;
    }

    /**
     * 判斷是否是不匹配
     */
    public boolean isUnmatch() {
        return this.hitState == UNMATCH;
    }

    /**
     *
     */
    public void setUnmatch() {
        this.hitState = UNMATCH;
    }

    public DictSegment getMatchedDictSegment() {
        return matchedDictSegment;
    }

    public void setMatchedDictSegment(DictSegment matchedDictSegment) {
        this.matchedDictSegment = matchedDictSegment;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

}
