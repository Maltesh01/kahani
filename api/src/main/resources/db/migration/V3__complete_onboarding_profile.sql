ALTER TABLE profiles
    ALTER COLUMN story TYPE TEXT,
    ALTER COLUMN interests TYPE VARCHAR(2000),
    ADD COLUMN what_here_for VARCHAR(500),
    ADD COLUMN occupation VARCHAR(120),
    ADD COLUMN native_city VARCHAR(100),
    ADD COLUMN languages VARCHAR(500),
    ADD COLUMN interest_notes TEXT,
    ADD COLUMN values_note TEXT;
