package hm.orz.chaos114.android.tumekyouen.model;

import java.util.ArrayList;
import java.util.List;

public class GameModel {

	private int size;

	private String startState;

	private List<Point> stonePoints = new ArrayList<Point>();

	private List<Point> whiteStonePoints = new ArrayList<Point>();

	public GameModel(int size, String startState) {
		this.size = size;
		this.startState = startState;

		char[] states = startState.toCharArray();
		for (int i = 0; i < states.length; i++) {
			if (states[i] == '0') {
				continue;
			}
			int col = i % size;
			int row = i / size;
			stonePoints.add(new Point(col, row));
		}
	}

	public int getSize() {
		return size;
	}

	public String getStartState() {
		return startState;
	}

	public void switchColor(int x, int y) {
		Point p = new Point(x, y);
		if (whiteStonePoints.contains(p)) {
			whiteStonePoints.remove(p);
		} else {
			whiteStonePoints.add(p);
		}
	}

	public boolean hasStone(int x, int y) {
		return stonePoints.contains(new Point(x, y));
	}

	public int getWhiteStoneCount() {
		return whiteStonePoints.size();
	}

	public KyouenData isKyouen() {
		if (whiteStonePoints.size() < 4) {
			return null;
		}
		Point p1 = whiteStonePoints.get(0);
		Point p2 = whiteStonePoints.get(1);
		Point p3 = whiteStonePoints.get(2);
		Point p4 = whiteStonePoints.get(3);

		KyouenData data = isKyouen(p1, p2, p3, p4);
		if (data != null) {
			return data;
		}

		return null;
	}

	public KyouenData isKyouen(Point p1, Point p2, Point p3, Point p4) {
		// p1,p2の垂直二等分線を求める
		Line l12 = getMidperpendicular(p1, p2);
		// p2,p3の垂直二等分線を求める
		Line l23 = getMidperpendicular(p2, p3);

		// 交点を求める
		Point intersection123 = getIntersection(l12, l23);
		if (intersection123 == null) {
			// p1,p2,p3が直線上に存在する場合
			Line l34 = getMidperpendicular(p3, p4);
			Point intersection234 = getIntersection(l23, l34);
			if (intersection234 == null) {
				// p2,p3,p4が直線状に存在する場合
				return new KyouenData(p1, p2, p3, p4, new Line(p1, p2));
			}
		} else {
			double dist1 = getDistance(p1, intersection123);
			double dist2 = getDistance(p4, intersection123);
			if (Math.abs(dist1 - dist2) < 0.0000001) {
				return new KyouenData(p1, p2, p3, p4, intersection123, dist1);
			}
		}
		return null;
	}

	/**
	 * 2点間の距離を求める。
	 * 
	 * @param p1 座標1
	 * @param p2 座標2
	 * @return 距離
	 */
	public double getDistance(Point p1, Point p2) {
		Point dist = p1.difference(p2);

		return dist.getAbs();
	}

	/**
	 * 2直線の交点を求める。
	 * 
	 * @param l1 直線1
	 * @param l2 直線2
	 * @return 交点座標（交点が存在しない場合、null）
	 */
	public Point getIntersection(Line l1, Line l2) {
		double f1 = l1.p2.x - l1.p1.x;
		double g1 = l1.p2.y - l1.p1.y;
		double f2 = l2.p2.x - l2.p1.x;
		double g2 = l2.p2.y - l2.p1.y;

		double det = f2 * g1 - f1 * g2;
		if (det == 0) {
			return null;
		}

		double dx = l2.p1.x - l1.p1.x;
		double dy = l2.p1.y - l1.p1.y;
		double t1 = (f2 * dy - g2 * dx) / det;

		return new Point(l1.p1.x + f1 * t1, l1.p1.y + g1 * t1);
	}

	/**
	 * 2点の垂直二等分線を求める。
	 * 
	 * @param p1 座標1
	 * @param p2 座標2
	 * @return 垂直二等分線
	 */
	public Line getMidperpendicular(Point p1, Point p2) {
		Point midpoint = getMidpoint(p1, p2);
		Point dif = p1.difference(p2);
		Point gradient = new Point(dif.y, -1 * dif.x);

		Line midperpendicular = new Line(midpoint, midpoint.sum(gradient));
		return midperpendicular;
	}

	/**
	 * 中点を求める。
	 * 
	 * @param p1 座標1
	 * @param p2 座標2
	 * @return 中点座標
	 */
	public Point getMidpoint(Point p1, Point p2) {

		Point midpoint = new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
		return midpoint;
	}
}
