package com.newman.shortcutbar.util;

import java.util.Comparator;

import com.newman.shortcutbar.vo.ShortcutItem;

public class ShortcutItemCompare implements Comparator<ShortcutItem> {

	@Override
	public int compare(ShortcutItem lhs, ShortcutItem rhs) {
		int lhsOrder = lhs.getOrder();
		int rhsOrder = rhs.getOrder();
		if (lhsOrder > rhsOrder) {
			return 1; 
		} else if (lhsOrder < rhsOrder) {
			return -1;
		} else {
			return 0;
		}
	}
}
