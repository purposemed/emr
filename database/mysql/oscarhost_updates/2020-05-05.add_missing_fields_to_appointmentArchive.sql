ALTER TABLE appointmentArchive ADD COLUMN IF NOT EXISTS isVirtual TINYINT(4) DEFAULT 0 AFTER urgency;
ALTER TABLE appointmentArchive ADD COLUMN IF NOT EXISTS reasonCode INT(11) AFTER notes;