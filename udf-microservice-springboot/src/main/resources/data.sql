-- Initialize field types
INSERT INTO field_type (type, display_name, validation_rules) VALUES
('TEXT', 'Text Field', '{"maxLength": 1000, "pattern": null}'),
('NUMBER', 'Number Field', '{"min": null, "max": null, "decimalPlaces": 2}'),
('DATE', 'Date Field', '{"format": "yyyy-MM-dd"}'),
('BOOLEAN', 'Boolean Field', '{}'),
('DROPDOWN', 'Dropdown Field', '{"options": []}')
ON CONFLICT (type) DO NOTHING;

-- Initialize reportable fields for customer entity
INSERT INTO reportable_fields (field_name, source_column, data_type, is_aggregatable, is_filterable, entity) VALUES
('id', 'id', 'number', false, true, 'customer'),
('segment', 'segment', 'text', false, true, 'customer'),
('risk_score', 'risk_score', 'number', true, true, 'customer'),
('created_at', 'created_at', 'date', false, true, 'customer'),
('updated_at', 'updated_at', 'date', false, true, 'customer')
ON CONFLICT DO NOTHING;

-- Initialize reportable fields for invoice entity
INSERT INTO reportable_fields (field_name, source_column, data_type, is_aggregatable, is_filterable, entity) VALUES
('id', 'id', 'number', false, true, 'invoice'),
('customer_id', 'customer_id', 'number', false, true, 'invoice'),
('amount', 'amount', 'number', true, true, 'invoice'),
('status', 'status', 'text', false, true, 'invoice'),
('created_at', 'created_at', 'date', false, true, 'invoice'),
('updated_at', 'updated_at', 'date', false, true, 'invoice')
ON CONFLICT DO NOTHING;