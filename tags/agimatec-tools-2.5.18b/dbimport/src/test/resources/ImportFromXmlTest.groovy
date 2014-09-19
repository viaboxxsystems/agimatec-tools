import com.agimatec.utility.fileimport.Importer
import com.agimatec.utility.fileimport.groovy.XmlSlurperSpec

Integer counter = new Integer(0);

def spec = new XmlSlurperSpec(
{ doc -> doc.sample },
{ processor ->
  def sample = processor.current;
  processor.log('element: ' + processor.rowCount + ' with: ' + sample);
  counter = new Integer(counter.intValue()+1);
});

def importer = new Importer(spec);
importer.importFrom(Importer.class.getClassLoader().getResourceAsStream("testimport.xml"));

return counter;