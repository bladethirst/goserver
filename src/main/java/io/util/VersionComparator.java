package io.util;

public class VersionComparator {
	public static int compareVersion(String version1, String version2) {
		String[] ver1s = version1.split("\\.");
		String[] ver2s = version2.split("\\.");
		int len = Math.min(ver1s.length, ver2s.length);
		for (int i = 0; i < len; ++i) {
			int v1 = Integer.valueOf(ver1s[i]).intValue();
			int v2 = Integer.valueOf(ver2s[i]).intValue();
			if (v1 < v2)
				return -1;
			if (v1 > v2)
				return 1;
		}
		if (ver1s.length > ver2s.length) {
			for (int i = ver2s.length; i < ver1s.length; ++i)
				if (Integer.valueOf(ver1s[i]).intValue() > 0)
					return 1;
		} else if (ver1s.length < ver2s.length) {
			for (int i = ver1s.length; i < ver2s.length; ++i) {
				if (Integer.valueOf(ver2s[i]).intValue() > 0)
					return -1;
			}
		}
		return 0;
	}
}