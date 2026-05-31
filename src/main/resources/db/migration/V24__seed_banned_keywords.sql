-- Seed banned keywords for testing content moderation
-- Categories: CHINH_TRI, KHIEU_DAM, BAO_LUC, HANG_CAM, NGON_TU_THU_HET

-- CHINH_TRI (Political violations)
INSERT INTO banned_keywords (keyword, violation_type, severity, description) VALUES
('chống phá nhà nước', 'CHINH_TRI', 'CRITICAL', 'Chống phá chính quyền'),
('đảo chính', 'CHINH_TRI', 'CRITICAL', 'Kích động đảo chính'),
('biểu tình', 'CHINH_TRI', 'HIGH', 'Kích động biểu tình trái phép'),
('lật đổ', 'CHINH_TRI', 'CRITICAL', 'Kích động lật đổ chính quyền');

-- KHIEU_DAM (Pornography/Obscenity)
INSERT INTO banned_keywords (keyword, violation_type, severity, description) VALUES
('khiêu dâm', 'KHIEU_DAM', 'CRITICAL', 'Nội dung khiêu dâm'),
('sex', 'KHIEU_DAM', 'HIGH', 'Nội dung tình dục'),
('porn', 'KHIEU_DAM', 'CRITICAL', 'Nội dung khiêu dâm'),
('nude', 'KHIEU_DAM', 'HIGH', 'Hình ảnh khỏa thân');

-- BAO_LUC (Violence)
INSERT INTO banned_keywords (keyword, violation_type, severity, description) VALUES
('giết người', 'BAO_LUC', 'CRITICAL', 'Kích động bạo lực'),
('tự tử', 'BAO_LUC', 'CRITICAL', 'Kích động tự tử'),
('đánh nhau', 'BAO_LUC', 'MEDIUM', 'Nội dung bạo lực'),
('máu me', 'BAO_LUC', 'HIGH', 'Nội dung kinh dị');

-- HANG_CAM (Illegal goods/services)
INSERT INTO banned_keywords (keyword, violation_type, severity, description) VALUES
('cá độ', 'HANG_CAM', 'CRITICAL', 'Quảng cáo cá độ'),
('đánh bạc', 'HANG_CAM', 'CRITICAL', 'Quảng cáo đánh bạc'),
('ma túy', 'HANG_CAM', 'CRITICAL', 'Buôn bán ma túy'),
('vũ khí', 'HANG_CAM', 'HIGH', 'Buôn bán vũ khí'),
('tài xỉu', 'HANG_CAM', 'CRITICAL', 'Cờ bạc trực tuyến');

-- NGON_TU_THU_HET (Hate speech/Fake news)
INSERT INTO banned_keywords (keyword, violation_type, severity, description) VALUES
('đồ ngu', 'NGON_TU_THU_HET', 'MEDIUM', 'Xúc phạm cá nhân'),
('chó má', 'NGON_TU_THU_HET', 'HIGH', 'Chửi thề tục tĩu'),
('fake news', 'NGON_TU_THU_HET', 'HIGH', 'Tin giả'),
('phân biệt chủng tộc', 'NGON_TU_THU_HET', 'CRITICAL', 'Phân biệt chủng tộc');

COMMENT ON TABLE banned_keywords IS 'Sample banned keywords for content moderation testing';
