
if (!db.columnExists('stallion_users', 'initialized')) {

db.execute('''
   ALTER TABLE stallion_users
       ADD COLUMN initialized bit(1) NOT NULL DEFAULT 0,
       ADD COLUMN fromGdprCountry bit(1) NOT NULL DEFAULT 0,
       ADD COLUMN acceptedTermsAt datetime NULL,
       ADD COLUMN acceptedTermsVersion VARCHAR(250) NOT NULL DEFAULT '',
       ADD COLUMN rightToBeForgottenInvokedAt datetime NULL;
''');

/// comment

}

/// comment