-- Update doa_rules table structure
-- 1. Change entity_id (UUID) to entity (VARCHAR)
-- 2. Remove valid_from and valid_to columns

DO $$
BEGIN
    -- Check if entity_id column exists (UUID type)
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'doa_rules'
        AND column_name = 'entity_id'
        AND data_type = 'uuid'
    ) THEN
        -- Drop foreign key constraint if it exists
        IF EXISTS (
            SELECT 1
            FROM information_schema.table_constraints
            WHERE table_schema = 'public'
            AND table_name = 'doa_rules'
            AND constraint_name = 'fk_doa_rules_entity'
        ) THEN
            ALTER TABLE public.doa_rules DROP CONSTRAINT fk_doa_rules_entity;
        END IF;

        -- Rename entity_id to entity_id_old
        ALTER TABLE public.doa_rules RENAME COLUMN entity_id TO entity_id_old;

        -- Add new entity column as VARCHAR
        ALTER TABLE public.doa_rules ADD COLUMN entity VARCHAR(255);

        -- Optionally: Copy data from entity_id_old to entity (convert UUID to text)
        -- UPDATE public.doa_rules SET entity = entity_id_old::TEXT WHERE entity_id_old IS NOT NULL;

        -- Make entity NOT NULL after data migration
        -- ALTER TABLE public.doa_rules ALTER COLUMN entity SET NOT NULL;

        -- Drop the old entity_id_old column after migration
        -- ALTER TABLE public.doa_rules DROP COLUMN entity_id_old;
    END IF;

    -- Check if entity column doesn't exist, create it
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'doa_rules'
        AND column_name = 'entity'
    ) THEN
        ALTER TABLE public.doa_rules ADD COLUMN entity VARCHAR(255);
    END IF;

    -- Remove valid_from column if it exists
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'doa_rules'
        AND column_name = 'valid_from'
    ) THEN
        ALTER TABLE public.doa_rules DROP COLUMN valid_from;
    END IF;

    -- Remove valid_to column if it exists
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'doa_rules'
        AND column_name = 'valid_to'
    ) THEN
        ALTER TABLE public.doa_rules DROP COLUMN valid_to;
    END IF;

    -- Drop the date range check constraint if it exists
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'public'
        AND table_name = 'doa_rules'
        AND constraint_name = 'chk_valid_date_range'
    ) THEN
        ALTER TABLE public.doa_rules DROP CONSTRAINT chk_valid_date_range;
    END IF;
END $$;

COMMENT ON COLUMN public.doa_rules.entity IS 'Entity name for DOA rule';

