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
package org.wltea.analyzer.core;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.dic.Hit;

/**
 *
 * 中文數量詞子分詞器
 */
class CN_QuantifierSegmenter implements ISegmenter {

    //子分詞器標籤
    static final String SEGMENTER_NAME = "QUAN_SEGMENTER";

    //中文數詞
    private static String Chn_Num = "一二兩三四五六七八九十零壹貳叄肆伍陸柒捌玖拾百千萬億拾佰仟萬億兆卅廿";//Cnum
    private static Set<Character> ChnNumberChars = new HashSet<Character>();

    static {
        char[] ca = Chn_Num.toCharArray();
        for (char nChar : ca) {
            ChnNumberChars.add(nChar);
        }
    }

    /*
     * 詞元的開始位置，
     * 同時作為子分詞器狀態標識
     * 當start > -1 時，標識當前的分詞器正在處理字元
     */
    private int nStart;
    /*
     * 記錄詞元結束位置
     * end記錄的是在詞元中最後一個出現的合理的數詞結束
     */
    private int nEnd;

    //待處理的量詞hit佇列
    private List<Hit> countHits;


    CN_QuantifierSegmenter() {
        nStart = -1;
        nEnd = -1;
        this.countHits = new LinkedList<Hit>();
    }

    /**
     * 分詞
     */
    public void analyze(AnalyzeContext context) {
        //處理中文數詞
        this.processCNumber(context);
        //處理中文量詞
        this.processCount(context);

        //判斷是否鎖定緩衝區
        if (this.nStart == -1 && this.nEnd == -1 && countHits.isEmpty()) {
            //對緩衝區解鎖
            context.unlockBuffer(SEGMENTER_NAME);
        } else {
            context.lockBuffer(SEGMENTER_NAME);
        }
    }


    /**
     * 重置子分詞器狀態
     */
    public void reset() {
        nStart = -1;
        nEnd = -1;
        countHits.clear();
    }

    /**
     * 處理數詞
     */
    private void processCNumber(AnalyzeContext context) {
        if (nStart == -1 && nEnd == -1) {//初始狀態
            if (CharacterUtil.CHAR_CHINESE == context.getCurrentCharType()
                    && ChnNumberChars.contains(context.getCurrentChar())) {
                //記錄數詞的起始、結束位置
                nStart = context.getCursor();
                nEnd = context.getCursor();
            }
        } else {//正在處理狀態
            if (CharacterUtil.CHAR_CHINESE == context.getCurrentCharType()
                    && ChnNumberChars.contains(context.getCurrentChar())) {
                //記錄數詞的結束位置
                nEnd = context.getCursor();
            } else {
                //輸出數詞
                this.outputNumLexeme(context);
                //重置頭尾指標
                nStart = -1;
                nEnd = -1;
            }
        }

        //緩衝區已經用完，還有尚未輸出的數詞
        if (context.isBufferConsumed() && (nStart != -1 && nEnd != -1)) {
            //輸出數詞
            outputNumLexeme(context);
            //重置頭尾指標
            nStart = -1;
            nEnd = -1;
        }
    }

    /**
     * 處理中文量詞
     * @param context
     */
    private void processCount(AnalyzeContext context) {
        // 判斷是否需要啟動量詞掃描
        if (!this.needCountScan(context)) {
            return;
        }

        if (CharacterUtil.CHAR_CHINESE == context.getCurrentCharType()) {

            //優先處理countHits中的hit
            if (!this.countHits.isEmpty()) {
                //處理詞段佇列
                Hit[] tmpArray = this.countHits.toArray(new Hit[this.countHits.size()]);
                for (Hit hit : tmpArray) {
                    hit = Dictionary.getSingleton().matchWithHit(context.getSegmentBuff(), context.getCursor(), hit);
                    if (hit.isMatch()) {
                        //輸出當前的詞
                        Lexeme newLexeme = new Lexeme(context.getBufferOffset(), hit.getBegin(), context.getCursor() - hit.getBegin() + 1, Lexeme.TYPE_COUNT);
                        context.addLexeme(newLexeme);

                        if (!hit.isPrefix()) {//不是詞字首，hit不需要繼續匹配，移除
                            this.countHits.remove(hit);
                        }

                    } else if (hit.isUnmatch()) {
                        //hit不是詞，移除
                        this.countHits.remove(hit);
                    }
                }
            }

            //*********************************
            //對當前指標位置的字元進行單字匹配
            Hit singleCharHit = Dictionary.getSingleton().matchInQuantifierDict(context.getSegmentBuff(), context.getCursor(), 1);
            if (singleCharHit.isMatch()) {//首字成量詞詞
                //輸出當前的詞
                Lexeme newLexeme = new Lexeme(context.getBufferOffset(), context.getCursor(), 1, Lexeme.TYPE_COUNT);
                context.addLexeme(newLexeme);

                //同時也是詞字首
                if (singleCharHit.isPrefix()) {
                    //字首匹配則放入hit列表
                    this.countHits.add(singleCharHit);
                }
            } else if (singleCharHit.isPrefix()) {//首字為量詞字首
                //字首匹配則放入hit列表
                this.countHits.add(singleCharHit);
            }


        } else {
            //輸入的不是中文字元
            //清空未成形的量詞
            this.countHits.clear();
        }

        //緩衝區資料已經讀完，還有尚未輸出的量詞
        if (context.isBufferConsumed()) {
            //清空未成形的量詞
            this.countHits.clear();
        }
    }

    /**
     * 判斷是否需要掃描量詞
     * @return
     */
    private boolean needCountScan(AnalyzeContext context) {
        if ((nStart != -1 && nEnd != -1) || !countHits.isEmpty()) {
            //正在處理中文數詞,或者正在處理量詞
            return true;
        } else {
            //找到一個相鄰的數詞
            if (!context.getOrgLexemes().isEmpty()) {
                Lexeme l = context.getOrgLexemes().peekLast();
                if ((Lexeme.TYPE_CNUM == l.getLexemeType() || Lexeme.TYPE_ARABIC == l.getLexemeType())
                        && (l.getBegin() + l.getLength() == context.getCursor())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 新增數詞詞元到結果集
     * @param context
     */
    private void outputNumLexeme(AnalyzeContext context) {
        if (nStart > -1 && nEnd > -1) {
            //輸出數詞
            Lexeme newLexeme = new Lexeme(context.getBufferOffset(), nStart, nEnd - nStart + 1, Lexeme.TYPE_CNUM);
            context.addLexeme(newLexeme);

        }
    }

}
