-- Add valid_from and valid_to columns to doa_rules table if they don't exist

DO $$
BEGIN
    -- Add valid_from column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'doa_rules'
        AND column_name = 'valid_from'
    ) THEN
        ALTER TABLE public.doa_rules
        ADD COLUMN valid_from DATE;
    END IF;

    -- Add valid_to column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'doa_rules'
        AND column_name = 'valid_to'
    ) THEN
        ALTER TABLE public.doa_rules
        ADD COLUMN valid_to DATE;
    END IF;

    -- Add check constraint for valid date range if it doesn't exist
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.constraint_column_usage
        WHERE table_schema = 'public'
        AND table_name = 'doa_rules'
        AND constraint_name = 'chk_valid_date_range'
    ) THEN
        ALTER TABLE public.doa_rules
        ADD CONSTRAINT chk_valid_date_range CHECK (valid_from <= valid_to);
    END IF;
END $$;

COMMENT ON COLUMN public.doa_rules.valid_from IS 'Start date of DOA rule validity period';
COMMENT ON COLUMN public.doa_rules.valid_to IS 'End date of DOA rule validity period';

