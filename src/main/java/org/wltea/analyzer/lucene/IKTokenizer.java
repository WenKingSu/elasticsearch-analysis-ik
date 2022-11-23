/**
 * IK 中文分詞  版本 5.0.1
 * IK Analyzer release 5.0.1
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

 * 
 */
package org.wltea.analyzer.lucene;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

/**
 * IK分詞器 Lucene Tokenizer介面卡類
 * 相容Lucene 4.0版本
 */
public final class IKTokenizer extends Tokenizer {
	
	//IK分詞器實現
	private IKSegmenter _IKImplement;
	
	//詞元文字屬性
	private final CharTermAttribute termAtt;
	//詞元位移屬性
	private final OffsetAttribute offsetAtt;
	//詞元分類屬性（該屬性分類參考org.wltea.analyzer.core.Lexeme中的分類常量）
	private final TypeAttribute typeAtt;
	//記錄最後一個詞元的結束位置
	private int endPosition;

   	private int skippedPositions;

   	private PositionIncrementAttribute posIncrAtt;


    /**
	 * Lucene 4.0 Tokenizer介面卡類建構函式
     */
	public IKTokenizer(Configuration configuration){
	    super();
	    offsetAtt = addAttribute(OffsetAttribute.class);
	    termAtt = addAttribute(CharTermAttribute.class);
	    typeAtt = addAttribute(TypeAttribute.class);
        posIncrAtt = addAttribute(PositionIncrementAttribute.class);

        _IKImplement = new IKSegmenter(input,configuration);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.TokenStream#incrementToken()
	 */
	@Override
	public boolean incrementToken() throws IOException {
		//清除所有的詞元屬性
		clearAttributes();
        skippedPositions = 0;

        Lexeme nextLexeme = _IKImplement.next();
		if(nextLexeme != null){
            posIncrAtt.setPositionIncrement(skippedPositions +1 );

			//將Lexeme轉成Attributes
			//設定詞元文字
			termAtt.append(nextLexeme.getLexemeText());
			//設定詞元長度
			termAtt.setLength(nextLexeme.getLength());
			//設定詞元位移
            offsetAtt.setOffset(correctOffset(nextLexeme.getBeginPosition()), correctOffset(nextLexeme.getEndPosition()));

            //記錄分詞的最後位置
			endPosition = nextLexeme.getEndPosition();
			//記錄詞元分類
			typeAtt.setType(nextLexeme.getLexemeTypeString());			
			//返會true告知還有下個詞元
			return true;
		}
		//返會false告知詞元輸出完畢
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.lucene.analysis.Tokenizer#reset(java.io.Reader)
	 */
	@Override
	public void reset() throws IOException {
		super.reset();
		_IKImplement.reset(input);
        skippedPositions = 0;
	}	
	
	@Override
	public final void end() throws IOException {
        super.end();
	    // set final offset
		int finalOffset = correctOffset(this.endPosition);
		offsetAtt.setOffset(finalOffset, finalOffset);
        posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
	}
}
