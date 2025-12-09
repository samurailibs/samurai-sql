package jp.dodododo.sql.paging;

/**
 * 
 * @author Satoshi Kimura
 */
public class Paging extends LimitOffset {

	private static final long serialVersionUID = 1291432066111220692L;

	private long pageNo;

	/**
	 * 
	 * @param limit 件数
	 * @param pageNo ページ番号(0スタート) 
	 */
	public Paging(long limit, long pageNo) {
		super(limit, pageNo * limit);
		this.pageNo = pageNo;
	}

	public long getPageNo() {
		return pageNo;
	}
}
