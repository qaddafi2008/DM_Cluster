/**
 * Copyright (C) 2012 why
 */
package why.dm.util;

import why.dm.cluster.Document;



/**
 * 文档指针与距离类
 *
 * @author qinhuiwang
 * @version $Rev$ $Date$
 */
public class DocumentDouble implements Comparable<DocumentDouble> {
	public Document document; // 拥有DocumentDouble对象的文档，和该文档来计算距离
	public Double distance; // 2文档的距离距离

	public DocumentDouble(Document docIndex, Double distance) {
		this.document = docIndex;
		this.distance = distance;
	}

	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DocumentDouble o) {
		return distance <= o.distance ? 1 : -1;
	}
}
