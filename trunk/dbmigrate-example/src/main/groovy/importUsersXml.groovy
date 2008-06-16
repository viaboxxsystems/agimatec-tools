import com.agimatec.commons.config.ConfigManager
import com.agimatec.utility.fileimport.*
import com.agimatec.utility.fileimport.groovy.XmlSlurperSpec
import groovy.sql.Sql

def filename = params[0];
def jdbcConnection = tool.targetDatabase.connection;
def su = SqlUtil.forConnection(jdbcConnection);
su.defSequence('id', 'hibernate_sequence');
ImportController controller = new ImportController(jdbcConnection, su, 'hibernate_sequence');
ImportControl imp = new ImportControl();
imp.setFileName (filename);
imp.setImportName ("Users");
long iid = controller.join(imp); // wait for other imports of that kind until finished...

def Sql db = new Sql(jdbcConnection);

String addressInsert = """
    INSERT INTO address (address_id, field_1, field_2, field_3, zip, city, country)
    values (?, ?, ?, ?, ?, ?, ?)
""";

String userInsert = """
    INSERT INTO user_core (registration_time, user_id, email, mobile_number, role_id, address_id, first_name, last_name, user_identification, gender, state)
     values(now(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
""";

def spec = new XmlSlurperSpec(
{ doc -> doc.user },
{ processor ->
  def user = processor.current;
  
  processor.log('element: ' + processor.rowCount + ' with: ' + user);
  long id = su.nextVal(jdbcConnection, 'id');

  def addressId;
  def countryCode = su.trim(user.country.text());
  if(countryCode == null) addressId = null; else addressId = new Long(id);

  if(addressId != null) {
      def affected;
      affected  = db.executeUpdate (addressInsert,
        [id,su.trim(user.address1.text()),su.trim(user.address2.text()),
        su.trim(user.address3.text()),su.trim(user.zip.text()),
        su.trim(user.city.text()),countryCode]);
      if(affected == 0) {
       throw new ImporterException("no row inserted: " + addressInsert, false);
      }
  }

  def gender = su.trim(user.gender.text());
  if('Mr'==gender || 'Mr.'==gender) gender = 'MALE';
  else if('Mrs'==gender || 'Mrs'==gender) gender = 'FEMALE';
  else gender = null;
  def status = su.trim(user.status.text());
  if(status == null) status = 'OK';
  
  affected = db.executeUpdate(userInsert, [id, su.trim(user.mail.text()),
    su.trim(user.mobile.text()), user.role.toInteger(), addressId,
    su.trim(user.firstname.text()), su.trim(user.lastname.text()), su.trim(user.id.text()),
    gender, status]);
  if(affected == 0) {
     throw new ImporterException("no row inserted: " + userInsert, false);
   }

});

def importer = new Importer(spec);
try {
    Reader reader = new InputStreamReader(ConfigManager.toURL(filename).openStream(),
    java.nio.charset.Charset.forName('ISO-8859-1'));
    importer.importFrom(reader);
} finally {
    controller.end(iid, importer);  // mark import as finished
}
