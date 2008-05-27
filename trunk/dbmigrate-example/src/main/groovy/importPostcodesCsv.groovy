import com.agimatec.utility.fileimport.*;
import com.agimatec.commons.config.*;
import com.agimatec.utility.fileimport.groovy.*;
import java.io.*;
import groovy.sql.*;
import org.apache.commons.lang.WordUtils;

def filename = params[0];
def jdbcConnection = tool.targetDatabase.connection;
def su = SqlUtil.forConnection(jdbcConnection);
su.defDate('Date', 'yyyy-MM-dd');
su.defSequence('id', 'hibernate_sequence');
ImportController controller = new ImportController(jdbcConnection, su, 'hibernate_sequence');
controller.join(filename); // wait for other imports of that kind until finished...

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
    controller.end(filename, importer);  // mark import as finished
}
return true;