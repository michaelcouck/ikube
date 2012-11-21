package ikube.mock;

import ikube.IConstants;

import java.util.Arrays;
import java.util.Collection;

import mockit.Mock;
import mockit.MockClass;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.ReaderUtil;

@MockClass(realClass = ReaderUtil.class)
public class ReaderUtilMock {

	@Mock
	public static Collection<String> getIndexedFields(IndexReader reader) {
		Collection<String> indexedFields = Arrays.asList(IConstants.ID, IConstants.FRAGMENT, IConstants.CONTENTS, IConstants.NAME);
		return indexedFields;
	}

}
