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

import java.util.Stack;
import java.util.TreeSet;

/**
 * IK分詞歧義裁決器
 */
class IKArbitrator {

    IKArbitrator() {

    }

    /**
     * 分詞歧義處理
     //	 * @param orgLexemes
     * @param useSmart
     */
    void process(AnalyzeContext context, boolean useSmart) {
        QuickSortSet orgLexemes = context.getOrgLexemes();
        Lexeme orgLexeme = orgLexemes.pollFirst();

        LexemePath crossPath = new LexemePath();
        while (orgLexeme != null) {
            if (!crossPath.addCrossLexeme(orgLexeme)) {
                //找到與crossPath不相交的下一個crossPath
                if (crossPath.size() == 1 || !useSmart) {
                    //crossPath沒有歧義 或者 不做歧義處理
                    //直接輸出當前crossPath
                    context.addLexemePath(crossPath);
                } else {
                    //對當前的crossPath進行歧義處理
                    QuickSortSet.Cell headCell = crossPath.getHead();
                    LexemePath judgeResult = this.judge(headCell, crossPath.getPathLength());
                    //輸出歧義處理結果judgeResult
                    context.addLexemePath(judgeResult);
                }

                //把orgLexeme加入新的crossPath中
                crossPath = new LexemePath();
                crossPath.addCrossLexeme(orgLexeme);
            }
            orgLexeme = orgLexemes.pollFirst();
        }


        //處理最後的path
        if (crossPath.size() == 1 || !useSmart) {
            //crossPath沒有歧義 或者 不做歧義處理
            //直接輸出當前crossPath
            context.addLexemePath(crossPath);
        } else {
            //對當前的crossPath進行歧義處理
            QuickSortSet.Cell headCell = crossPath.getHead();
            LexemePath judgeResult = this.judge(headCell, crossPath.getPathLength());
            //輸出歧義處理結果judgeResult
            context.addLexemePath(judgeResult);
        }
    }

    /**
     * 歧義識別
     * @param lexemeCell 歧義路徑連結串列頭
     * @param fullTextLength 歧義路徑文字長度
     * @return
     */
    private LexemePath judge(QuickSortSet.Cell lexemeCell, int fullTextLength) {
        //候選路徑集合
        TreeSet<LexemePath> pathOptions = new TreeSet<LexemePath>();
        //候選結果路徑
        LexemePath option = new LexemePath();

        //對crossPath進行一次遍歷,同時返回本次遍歷中有衝突的Lexeme棧
        Stack<QuickSortSet.Cell> lexemeStack = this.forwardPath(lexemeCell, option);

        //當前詞元鏈並非最理想的，加入候選路徑集合
        pathOptions.add(option.copy());

        //存在歧義詞，處理
        QuickSortSet.Cell c = null;
        while (!lexemeStack.isEmpty()) {
            c = lexemeStack.pop();
            //回滾詞元鏈
            this.backPath(c.getLexeme(), option);
            //從歧義詞位置開始，遞迴，生成可選方案
            this.forwardPath(c, option);
            pathOptions.add(option.copy());
        }

        //返回集合中的最優方案
        return pathOptions.first();

    }

    /**
     * 向前遍歷，新增詞元，構造一個無歧義詞元組合
     //	 * @param LexemePath path
     * @return
     */
    private Stack<QuickSortSet.Cell> forwardPath(QuickSortSet.Cell lexemeCell, LexemePath option) {
        //發生衝突的Lexeme棧
        Stack<QuickSortSet.Cell> conflictStack = new Stack<QuickSortSet.Cell>();
        QuickSortSet.Cell c = lexemeCell;
        //迭代遍歷Lexeme連結串列
        while (c != null && c.getLexeme() != null) {
            if (!option.addNotCrossLexeme(c.getLexeme())) {
                //詞元交叉，新增失敗則加入lexemeStack棧
                conflictStack.push(c);
            }
            c = c.getNext();
        }
        return conflictStack;
    }

    /**
     * 回滾詞元鏈，直到它能夠接受指定的詞元
     //	 * @param lexeme
     * @param l
     */
    private void backPath(Lexeme l, LexemePath option) {
        while (option.checkCross(l)) {
            option.removeTail();
        }

    }

}
