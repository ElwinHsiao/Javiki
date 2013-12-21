package norg.javiki.parser.test;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import norg.javiki.parser.GeneralParseMachine;
import norg.javiki.parser.ParseInterface;
import norg.javiki.parser.XmlParseInterface;
import norg.javiki.parser.XmlUtils;
import junit.framework.TestCase;

public class GeneralParseMachineTest extends TestCase {
	
	private GeneralParseMachine<Object> mGeneralParseMachine;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mGeneralParseMachine = new GeneralParseMachine<Object>();
	}
//	
//	@Override
//	protected void tearDown() throws Exception {
//		super.tearDown();
//    }
	public void testModeONCE() {
		mGeneralParseMachine.setParserListener(new XmlParseInterface<Object>() {

			@Override
			public void done(long id, Object result) {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected Object parse(XmlPullParser parser) {
				// TODO Auto-generated method stub
				XmlUtils.printXml(parser, 0);
				return null;
			}
		});
		mGeneralParseMachine.parse("http://204.45.127.242:8778/yuanltech/data/record/column.xml");
	}
	
}
