package ikube.mock;

import ikube.IConstants;
import mockit.Mock;
import mockit.MockClass;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;

import java.util.Arrays;
import java.util.Collection;

@MockClass(realClass = DirectoryReader.class)
public class ReaderUtilMock {

    @Mock
    public static Collection<String> getIndexedFields(IndexReader reader) {
        return Arrays.asList(IConstants.ID, IConstants.FRAGMENT, IConstants.CONTENTS, IConstants.NAME);
    }

}
