/**
 * IK 中文分詞  版本 5.0
 * IK Analyzer release 5.0
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 原始碼由林良益(linliangyi2005@gmail.com)提供
 * 版權宣告 2012，烏龍茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 * 
 */
package org.wltea.analyzer.core;

import java.util.Arrays;

/**
 * 
 * 英文字元及阿拉伯數字子分詞器
 */
class LetterSegmenter implements ISegmenter {
	
	//子分詞器標籤
	static final String SEGMENTER_NAME = "LETTER_SEGMENTER";
	//連結符號
	private static final char[] Letter_Connector = new char[]{'#' , '&' , '+' , '-' , '.' , '@' , '_'};
	
	//數字符號
	private static final char[] Num_Connector = new char[]{',' , '.'};
	
	/*
	 * 詞元的開始位置，
	 * 同時作為子分詞器狀態標識
	 * 當start > -1 時，標識當前的分詞器正在處理字元
	 */
	private int start;
	/*
	 * 記錄詞元結束位置
	 * end記錄的是在詞元中最後一個出現的Letter但非Sign_Connector的字元的位置
	 */
	private int end;
	
	/*
	 * 字母起始位置
	 */
	private int englishStart;

	/*
	 * 字母結束位置
	 */
	private int englishEnd;
	
	/*
	 * 阿拉伯數字起始位置
	 */
	private int arabicStart;
	
	/*
	 * 阿拉伯數字結束位置
	 */
	private int arabicEnd;
	
	LetterSegmenter(){
		Arrays.sort(Letter_Connector);
		Arrays.sort(Num_Connector);
		this.start = -1;
		this.end = -1;
		this.englishStart = -1;
		this.englishEnd = -1;
		this.arabicStart = -1;
		this.arabicEnd = -1;
	}


	/* (non-Javadoc)
	 * @see org.wltea.analyzer.core.ISegmenter#analyze(org.wltea.analyzer.core.AnalyzeContext)
	 */
	public void analyze(AnalyzeContext context) {
		boolean bufferLockFlag = false;
		//處理英文字母
		bufferLockFlag = this.processEnglishLetter(context) || bufferLockFlag;
		//處理阿拉伯字母
		bufferLockFlag = this.processArabicLetter(context) || bufferLockFlag;
		//處理混合字母(這個要放最後處理，可以透過QuickSortSet排除重複)
		bufferLockFlag = this.processMixLetter(context) || bufferLockFlag;
		
		//判斷是否鎖定緩衝區
		if(bufferLockFlag){
			context.lockBuffer(SEGMENTER_NAME);
		}else{
			//對緩衝區解鎖
			context.unlockBuffer(SEGMENTER_NAME);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.wltea.analyzer.core.ISegmenter#reset()
	 */
	public void reset() {
		this.start = -1;
		this.end = -1;
		this.englishStart = -1;
		this.englishEnd = -1;
		this.arabicStart = -1;
		this.arabicEnd = -1;
	}	
	
	/**
	 * 處理數字字母混合輸出
	 * 如：windos2000 | linliangyi2005@gmail.com
//	 * @param input
	 * @param context
	 * @return
	 */
	private boolean processMixLetter(AnalyzeContext context){
		boolean needLock = false;
		
		if(this.start == -1){//當前的分詞器尚未開始處理字元
			if(CharacterUtil.CHAR_ARABIC == context.getCurrentCharType()
					|| CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()){
				//記錄起始指標的位置,標明分詞器進入處理狀態
				this.start = context.getCursor();
				this.end = start;
			}
			
		}else{//當前的分詞器正在處理字元			
			if(CharacterUtil.CHAR_ARABIC == context.getCurrentCharType()
					|| CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()){
				//記錄下可能的結束位置
				this.end = context.getCursor();
				
			}else if(CharacterUtil.CHAR_USELESS == context.getCurrentCharType()
						&& this.isLetterConnector(context.getCurrentChar())){
				//記錄下可能的結束位置
				this.end = context.getCursor();
			}else{
				//遇到非Letter字元，輸出詞元
				Lexeme newLexeme = new Lexeme(context.getBufferOffset() , this.start , this.end - this.start + 1 , Lexeme.TYPE_LETTER);
				context.addLexeme(newLexeme);
				this.start = -1;
				this.end = -1;
			}			
		}
		
		//判斷緩衝區是否已經讀完
		if(context.isBufferConsumed() && (this.start != -1 && this.end != -1)){
            //緩衝以讀完，輸出詞元
            Lexeme newLexeme = new Lexeme(context.getBufferOffset() , this.start , this.end - this.start + 1 , Lexeme.TYPE_LETTER);
            context.addLexeme(newLexeme);
            this.start = -1;
            this.end = -1;
		}
		
		//判斷是否鎖定緩衝區
		if(this.start == -1 && this.end == -1){
			//對緩衝區解鎖
			needLock = false;
		}else{
			needLock = true;
		}
		return needLock;
	}
	
	/**
	 * 處理純英文字母輸出
	 * @param context
	 * @return
	 */
	private boolean processEnglishLetter(AnalyzeContext context){
		boolean needLock = false;
		
		if(this.englishStart == -1){//當前的分詞器尚未開始處理英文字元	
			if(CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()){
				//記錄起始指標的位置,標明分詞器進入處理狀態
				this.englishStart = context.getCursor();
				this.englishEnd = this.englishStart;
			}
		}else {//當前的分詞器正在處理英文字元	
			if(CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()){
				//記錄當前指標位置為結束位置
				this.englishEnd =  context.getCursor();
			}else{
				//遇到非English字元,輸出詞元
				Lexeme newLexeme = new Lexeme(context.getBufferOffset() , this.englishStart , this.englishEnd - this.englishStart + 1 , Lexeme.TYPE_ENGLISH);
				context.addLexeme(newLexeme);
				this.englishStart = -1;
				this.englishEnd= -1;
			}
		}
		
		//判斷緩衝區是否已經讀完
		if(context.isBufferConsumed() && (this.englishStart != -1 && this.englishEnd != -1)){
            //緩衝以讀完，輸出詞元
            Lexeme newLexeme = new Lexeme(context.getBufferOffset() , this.englishStart , this.englishEnd - this.englishStart + 1 , Lexeme.TYPE_ENGLISH);
            context.addLexeme(newLexeme);
            this.englishStart = -1;
            this.englishEnd= -1;
		}	
		
		//判斷是否鎖定緩衝區
		if(this.englishStart == -1 && this.englishEnd == -1){
			//對緩衝區解鎖
			needLock = false;
		}else{
			needLock = true;
		}
		return needLock;			
	}
	
	/**
	 * 處理阿拉伯數字輸出
	 * @param context
	 * @return
	 */
	private boolean processArabicLetter(AnalyzeContext context){
		boolean needLock = false;
		
		if(this.arabicStart == -1){//當前的分詞器尚未開始處理數字字元	
			if(CharacterUtil.CHAR_ARABIC == context.getCurrentCharType()){
				//記錄起始指標的位置,標明分詞器進入處理狀態
				this.arabicStart = context.getCursor();
				this.arabicEnd = this.arabicStart;
			}
		}else {//當前的分詞器正在處理數字字元	
			if(CharacterUtil.CHAR_ARABIC == context.getCurrentCharType()){
				//記錄當前指標位置為結束位置
				this.arabicEnd = context.getCursor();
			}else if(CharacterUtil.CHAR_USELESS == context.getCurrentCharType()
					&& this.isNumConnector(context.getCurrentChar())){
				//不輸出數字，但不標記結束
			}else{
				////遇到非Arabic字元,輸出詞元
				Lexeme newLexeme = new Lexeme(context.getBufferOffset() , this.arabicStart , this.arabicEnd - this.arabicStart + 1 , Lexeme.TYPE_ARABIC);
				context.addLexeme(newLexeme);
				this.arabicStart = -1;
				this.arabicEnd = -1;
			}
		}
		
		//判斷緩衝區是否已經讀完
		if(context.isBufferConsumed() && (this.arabicStart != -1 && this.arabicEnd != -1)){
            //生成已切分的詞元
            Lexeme newLexeme = new Lexeme(context.getBufferOffset() ,  this.arabicStart , this.arabicEnd - this.arabicStart + 1 , Lexeme.TYPE_ARABIC);
            context.addLexeme(newLexeme);
            this.arabicStart = -1;
            this.arabicEnd = -1;
		}
		
		//判斷是否鎖定緩衝區
		if(this.arabicStart == -1 && this.arabicEnd == -1){
			//對緩衝區解鎖
			needLock = false;
		}else{
			needLock = true;
		}
		return needLock;		
	}	

	/**
	 * 判斷是否是字母連線符號
	 * @param input
	 * @return
	 */
	private boolean isLetterConnector(char input){
		int index = Arrays.binarySearch(Letter_Connector, input);
		return index >= 0;
	}
	
	/**
	 * 判斷是否是數字連線符號
	 * @param input
	 * @return
	 */
	private boolean isNumConnector(char input){
		int index = Arrays.binarySearch(Num_Connector, input);
		return index >= 0;
	}
}
