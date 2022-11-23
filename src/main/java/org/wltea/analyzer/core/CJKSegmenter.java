
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

import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.dic.Hit;

import java.util.LinkedList;
import java.util.List;


/**
 *  中文-日韓文子分詞器
 */
class CJKSegmenter implements ISegmenter {

    //子分詞器標籤
    static final String SEGMENTER_NAME = "CJK_SEGMENTER";
    //待處理的分詞hit佇列
    private List<Hit> tmpHits;


    CJKSegmenter() {
        this.tmpHits = new LinkedList<Hit>();
    }

    /* (non-Javadoc)
     * @see org.wltea.analyzer.core.ISegmenter#analyze(org.wltea.analyzer.core.AnalyzeContext)
     */
    public void analyze(AnalyzeContext context) {
        if (CharacterUtil.CHAR_USELESS != context.getCurrentCharType()) {

            //優先處理tmpHits中的hit
            if (!this.tmpHits.isEmpty()) {
                //處理詞段佇列
                Hit[] tmpArray = this.tmpHits.toArray(new Hit[this.tmpHits.size()]);
                for (Hit hit : tmpArray) {
                    hit = Dictionary.getSingleton().matchWithHit(context.getSegmentBuff(), context.getCursor(), hit);
                    if (hit.isMatch()) {
                        //輸出當前的詞
                        Lexeme newLexeme = new Lexeme(context.getBufferOffset(), hit.getBegin(), context.getCursor() - hit.getBegin() + 1, Lexeme.TYPE_CNWORD);
                        context.addLexeme(newLexeme);

                        if (!hit.isPrefix()) {//不是詞字首，hit不需要繼續匹配，移除
                            this.tmpHits.remove(hit);
                        }

                    } else if (hit.isUnmatch()) {
                        //hit不是詞，移除
                        this.tmpHits.remove(hit);
                    }
                }
            }

            //*********************************
            //再對當前指標位置的字元進行單字匹配
            Hit singleCharHit = Dictionary.getSingleton().matchInMainDict(context.getSegmentBuff(), context.getCursor(), 1);
            if (singleCharHit.isMatch()) {//首字成詞
                //輸出當前的詞
                Lexeme newLexeme = new Lexeme(context.getBufferOffset(), context.getCursor(), 1, Lexeme.TYPE_CNWORD);
                context.addLexeme(newLexeme);

                //同時也是詞字首
                if (singleCharHit.isPrefix()) {
                    //字首匹配則放入hit列表
                    this.tmpHits.add(singleCharHit);
                }
            } else if (singleCharHit.isPrefix()) {//首字為詞字首
                //字首匹配則放入hit列表
                this.tmpHits.add(singleCharHit);
            }


        } else {
            //遇到CHAR_USELESS字元
            //清空佇列
            this.tmpHits.clear();
        }

        //判斷緩衝區是否已經讀完
        if (context.isBufferConsumed()) {
            //清空佇列
            this.tmpHits.clear();
        }

        //判斷是否鎖定緩衝區
        if (this.tmpHits.size() == 0) {
            context.unlockBuffer(SEGMENTER_NAME);

        } else {
            context.lockBuffer(SEGMENTER_NAME);
        }
    }

    /* (non-Javadoc)
     * @see org.wltea.analyzer.core.ISegmenter#reset()
     */
    public void reset() {
        //清空佇列
        this.tmpHits.clear();
    }

}
