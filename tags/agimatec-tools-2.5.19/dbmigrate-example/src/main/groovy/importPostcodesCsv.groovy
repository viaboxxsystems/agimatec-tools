import com.agimatec.commons.config.ConfigManager
import com.agimatec.utility.fileimport.*
import com.agimatec.utility.fileimport.groovy.LineImporterSpecGroovy
import groovy.sql.Sql
import org.apache.commons.lang.WordUtils

def filename = params[0];
def jdbcConnection = tool.targetDatabase.connection;
def su = SqlUtil.forConnection(jdbcConnection);
su.defDate('Date', 'yyyy-MM-dd');
su.defSequence('id', 'hibernate_sequence');
ImportController controller = new ImportController(jdbcConnection, su, 'hibernate_sequence');
ImportControl imp = new ImportControl();
imp.setFileName (filename);
imp.setImportName ("PostCodes");
long iid = controller.join(imp); // wait for other imports of that kind until finished...

def Sql db = new Sql(jdbcConnection);

String postcodeInsert = """
    INSERT INTO CV_Postcode(postcode_id, zip, description, valid_from, country)
    values(${su.get('id')}, ?, ?, ?, ?)
""";


def spec = new LineImporterSpecGroovy({ processor ->
 def row = processor.currentRow;
 processor.log('row: ' + processor.rowCount + '; insert postcode: ' + row);
 if(db.executeUpdate (postcodeInsert,
    [row.PostalCode,
    WordUtils.capitalizeFully(row.PostalCodeDescription),
    su.timestamp('Date', row.DateFrom), row.CountryCode]) == 0) {
      throw new ImporterException("no row inserted: " + postcodeInsert, false);
    }
});

def importer = new Importer(spec);
db.execute 'DELETE FROM CV_Postcode';
try {
    Reader reader = new InputStreamReader(ConfigManager.toURL(filename).openStream(),
     java.nio.charset.Charset.forName('ISO-8859-1'));
    importer.importFrom(reader);
} finally {
    controller.end(iid, importer);  // mark import as finished
}
return true;