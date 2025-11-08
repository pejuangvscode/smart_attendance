-- ==============================
-- STORED PROCEDURES FOR STATISTICS
-- ==============================

-- Function untuk mendapatkan statistik keseluruhan user
CREATE OR REPLACE FUNCTION get_user_statistics(p_user_id VARCHAR)
RETURNS TABLE(
    total_present BIGINT,
    total_late BIGINT,
    total_absent BIGINT,
    total_meetings BIGINT,
    attendance_percentage NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COALESCE(SUM(CASE WHEN a.status = 'present' THEN 1 ELSE 0 END), 0) as total_present,
        COALESCE(SUM(CASE WHEN a.status = 'late' THEN 1 ELSE 0 END), 0) as total_late,
        COALESCE(SUM(CASE WHEN a.status = 'absent' THEN 1 ELSE 0 END), 0) as total_absent,
        COALESCE(COUNT(a.attendance_id), 0) as total_meetings,
        COALESCE(
            ROUND(
                SUM(CASE WHEN a.status = 'present' THEN 1 ELSE 0 END) * 100.0 /
                NULLIF(COUNT(a.attendance_id), 0),
                2
            ),
            0
        ) as attendance_percentage
    FROM enrollments e
    LEFT JOIN attendances a ON e.enrollment_id = a.enrollment_id
    WHERE e.user_id = p_user_id;
END;
$$ LANGUAGE plpgsql;

-- Function untuk mendapatkan statistik per mata kuliah
CREATE OR REPLACE FUNCTION get_course_statistics(p_user_id VARCHAR)
RETURNS TABLE(
    course_code VARCHAR,
    course_name VARCHAR,
    total_pertemuan BIGINT,
    hadir BIGINT,
    terlambat BIGINT,
    tidak_hadir BIGINT,
    persentase_kehadiran NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        c.course_code,
        c.course_name,
        COUNT(a.attendance_id) as total_pertemuan,
        SUM(CASE WHEN a.status = 'present' THEN 1 ELSE 0 END) as hadir,
        SUM(CASE WHEN a.status = 'late' THEN 1 ELSE 0 END) as terlambat,
        SUM(CASE WHEN a.status = 'absent' THEN 1 ELSE 0 END) as tidak_hadir,
        ROUND(
            SUM(CASE WHEN a.status = 'present' THEN 1 ELSE 0 END) * 100.0 /
            NULLIF(COUNT(a.attendance_id), 0),
            2
        ) as persentase_kehadiran
    FROM enrollments e
    JOIN courses c ON e.course_id = c.course_id
    LEFT JOIN attendances a ON e.enrollment_id = a.enrollment_id
    WHERE e.user_id = p_user_id
    GROUP BY c.course_code, c.course_name, c.course_id
    ORDER BY c.course_code;
END;
$$ LANGUAGE plpgsql;
