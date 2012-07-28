package hm.orz.chaos114.android.tumekyouen.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TumeKyouenModel implements Serializable {
	
	public static final Integer CLEAR = 1;

	/** ステージ番号 */
	private int stageNo;

	/** ステージサイズ */
	private int size;

	/** ステージ上の石の配置（配置箇所には1、それ以外は0） */
	private String stage;

	/** 作者 */
	private String creator;

	/** クリアフラグ（クリア時に1、それ以外は0） */
	private int clearFlag;

	public int getStageNo() {
		return stageNo;
	}

	public void setStageNo(int stageNo) {
		this.stageNo = stageNo;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public int getClearFlag() {
		return clearFlag;
	}

	public void setClearFlag(int clearFlag) {
		this.clearFlag = clearFlag;
	}

}
