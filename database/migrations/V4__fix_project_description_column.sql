-- V4: Fix DetailedDescription column to LONGTEXT to support long project descriptions
-- This resolves "Data truncation: Data too long for column 'detailed_description'"

ALTER TABLE Projects
    MODIFY COLUMN DetailedDescription LONGTEXT NULL;

-- Also fix Services DetailedDescription if it exists
ALTER TABLE Services
    MODIFY COLUMN DetailedDescription LONGTEXT NULL;
