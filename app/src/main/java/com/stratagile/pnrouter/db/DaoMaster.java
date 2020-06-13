package com.stratagile.pnrouter.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import org.greenrobot.greendao.AbstractDaoMaster;
import org.greenrobot.greendao.database.StandardDatabase;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseOpenHelper;
import org.greenrobot.greendao.identityscope.IdentityScopeType;


// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/**
 * Master of DAO (schema version 104): knows all DAOs.
 */
public class DaoMaster extends AbstractDaoMaster {
    public static final int SCHEMA_VERSION = 104;

    /** Creates underlying database table using DAOs. */
    public static void createAllTables(Database db, boolean ifNotExists) {
        LocalFileItemDao.createTable(db, ifNotExists);
        EmailAttachEntityDao.createTable(db, ifNotExists);
        EmailContactsEntityDao.createTable(db, ifNotExists);
        GroupVerifyEntityDao.createTable(db, ifNotExists);
        EmailMessageEntityDao.createTable(db, ifNotExists);
        RouterUserEntityDao.createTable(db, ifNotExists);
        UserEntityDao.createTable(db, ifNotExists);
        RouterEntityDao.createTable(db, ifNotExists);
        RecentFileDao.createTable(db, ifNotExists);
        DraftEntityDao.createTable(db, ifNotExists);
        EmailConfigEntityDao.createTable(db, ifNotExists);
        LocalFileMenuDao.createTable(db, ifNotExists);
        EmailCidEntityDao.createTable(db, ifNotExists);
        ActiveEntityDao.createTable(db, ifNotExists);
        FriendEntityDao.createTable(db, ifNotExists);
        GroupEntityDao.createTable(db, ifNotExists);
        QLCAccountDao.createTable(db, ifNotExists);
        MessageEntityDao.createTable(db, ifNotExists);
        FileUploadItemDao.createTable(db, ifNotExists);
        SMSEntityDao.createTable(db, ifNotExists);
    }

    /** Drops underlying database table using DAOs. */
    public static void dropAllTables(Database db, boolean ifExists) {
        LocalFileItemDao.dropTable(db, ifExists);
        EmailAttachEntityDao.dropTable(db, ifExists);
        EmailContactsEntityDao.dropTable(db, ifExists);
        GroupVerifyEntityDao.dropTable(db, ifExists);
        EmailMessageEntityDao.dropTable(db, ifExists);
        RouterUserEntityDao.dropTable(db, ifExists);
        UserEntityDao.dropTable(db, ifExists);
        RouterEntityDao.dropTable(db, ifExists);
        RecentFileDao.dropTable(db, ifExists);
        DraftEntityDao.dropTable(db, ifExists);
        EmailConfigEntityDao.dropTable(db, ifExists);
        LocalFileMenuDao.dropTable(db, ifExists);
        EmailCidEntityDao.dropTable(db, ifExists);
        ActiveEntityDao.dropTable(db, ifExists);
        FriendEntityDao.dropTable(db, ifExists);
        GroupEntityDao.dropTable(db, ifExists);
        QLCAccountDao.dropTable(db, ifExists);
        MessageEntityDao.dropTable(db, ifExists);
        FileUploadItemDao.dropTable(db, ifExists);
        SMSEntityDao.dropTable(db, ifExists);
    }

    /**
     * WARNING: Drops all table on Upgrade! Use only during development.
     * Convenience method using a {@link DevOpenHelper}.
     */
    public static DaoSession newDevSession(Context context, String name) {
        Database db = new DevOpenHelper(context, name).getWritableDb();
        DaoMaster daoMaster = new DaoMaster(db);
        return daoMaster.newSession();
    }

    public DaoMaster(SQLiteDatabase db) {
        this(new StandardDatabase(db));
    }

    public DaoMaster(Database db) {
        super(db, SCHEMA_VERSION);
        registerDaoClass(LocalFileItemDao.class);
        registerDaoClass(EmailAttachEntityDao.class);
        registerDaoClass(EmailContactsEntityDao.class);
        registerDaoClass(GroupVerifyEntityDao.class);
        registerDaoClass(EmailMessageEntityDao.class);
        registerDaoClass(RouterUserEntityDao.class);
        registerDaoClass(UserEntityDao.class);
        registerDaoClass(RouterEntityDao.class);
        registerDaoClass(RecentFileDao.class);
        registerDaoClass(DraftEntityDao.class);
        registerDaoClass(EmailConfigEntityDao.class);
        registerDaoClass(LocalFileMenuDao.class);
        registerDaoClass(EmailCidEntityDao.class);
        registerDaoClass(ActiveEntityDao.class);
        registerDaoClass(FriendEntityDao.class);
        registerDaoClass(GroupEntityDao.class);
        registerDaoClass(QLCAccountDao.class);
        registerDaoClass(MessageEntityDao.class);
        registerDaoClass(FileUploadItemDao.class);
        registerDaoClass(SMSEntityDao.class);
    }

    public DaoSession newSession() {
        return new DaoSession(db, IdentityScopeType.Session, daoConfigMap);
    }

    public DaoSession newSession(IdentityScopeType type) {
        return new DaoSession(db, type, daoConfigMap);
    }

    /**
     * Calls {@link #createAllTables(Database, boolean)} in {@link #onCreate(Database)} -
     */
    public static abstract class OpenHelper extends DatabaseOpenHelper {
        public OpenHelper(Context context, String name) {
            super(context, name, SCHEMA_VERSION);
        }

        public OpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory, SCHEMA_VERSION);
        }

        @Override
        public void onCreate(Database db) {
            Log.i("greenDAO", "Creating tables for schema version " + SCHEMA_VERSION);
            createAllTables(db, false);
        }
    }

    /** WARNING: Drops all table on Upgrade! Use only during development. */
    public static class DevOpenHelper extends OpenHelper {
        public DevOpenHelper(Context context, String name) {
            super(context, name);
        }

        public DevOpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            dropAllTables(db, true);
            onCreate(db);
        }
    }

}
