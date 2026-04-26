package ninja.javahacker.test.annotimpler.convert;

import module java.base;
import module org.junit.jupiter.api;

public class TemporalConverterTest {

    @TestFactory
    public List<DynamicNode> testTemporalTypes() throws Exception {
        var h = new HeavyConverterTestSupport();
        var prefix1a = "[testTemporalTypes - From Date + Time + Timezone]";
        var prefix1b = "[testTemporalTypes - From String with Date + Time + Timezone]";
        var prefix2a = "[testTemporalTypes - From Date + Time, no Timezone]";
        var prefix2b = "[testTemporalTypes - From String with Date + Time, no Timezone]";
        var prefix3a = "[testTemporalTypes - From Only Date]";
        var prefix3b = "[testTemporalTypes - From String with Only Date]";
        var prefix4a = "[testTemporalTypes - From Time + Timezone, no Date]";
        var prefix4b = "[testTemporalTypes - From String with Time + Timezone, no Date]";
        var prefix5a = "[testTemporalTypes - From Only Time]";
        var prefix5b = "[testTemporalTypes - From String with Only Time]";

        var str1 = h.e(String.class, List.of(
                "2026-01-02 03:04:05 +06:13",
                "2025-10-31 13:14:15.123456 +11:55:44",
                "2022-04-12 08:07:06.123456789 -03:04:05",
                "2023-07-14 11:12:13.123 -00:12",
                "2021-10-04 13:14:15.12 -12:20",
                "2022-09-14 21:10:12.1 -04:10",
                "xxx xxx xxx"
        ));
        var str2 = str1.map(String.class, x -> x.split(" ")[0] + " " + x.split(" ")[1]);
        var str3 = str1.map(String.class, x -> x.split(" ")[0]);
        var str4 = str1.map(String.class, x -> x.split(" ")[1] + " " + x.split(" ")[2]);
        var str5 = str1.map(String.class, x -> x.split(" ")[1]);

        var r4str1A = str1.map(TestTypes.R4String.class, TestTypes.R4String::new);
        var r4str2A = str2.map(TestTypes.R4String.class, TestTypes.R4String::new);
        var r4str3A = str3.map(TestTypes.R4String.class, TestTypes.R4String::new);
        var r4str4A = str4.map(TestTypes.R4String.class, TestTypes.R4String::new);
        var r4str5A = str5.map(TestTypes.R4String.class, TestTypes.R4String::new);

        var r4str1B = str1.map(TestTypes.R4StringList.class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4str2B = str2.map(TestTypes.R4StringList.class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4str3B = str3.map(TestTypes.R4StringList.class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4str4B = str4.map(TestTypes.R4StringList.class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4str5B = str5.map(TestTypes.R4StringList.class, x -> new TestTypes.R4StringList(List.of(x)));

        var r4str1C = r4str1B.map(TestTypes.R4Record.class, TestTypes.R4Record::new);
        var r4str2C = r4str2B.map(TestTypes.R4Record.class, TestTypes.R4Record::new);
        var r4str3C = r4str3B.map(TestTypes.R4Record.class, TestTypes.R4Record::new);
        var r4str4C = r4str4B.map(TestTypes.R4Record.class, TestTypes.R4Record::new);
        var r4str5C = r4str5B.map(TestTypes.R4Record.class, TestTypes.R4Record::new);

        var r4str1D = str1.map(TestTypes.R4StringArray.class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4str2D = str2.map(TestTypes.R4StringArray.class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4str3D = str3.map(TestTypes.R4StringArray.class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4str4D = str4.map(TestTypes.R4StringArray.class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4str5D = str5.map(TestTypes.R4StringArray.class, x -> new TestTypes.R4StringArray(new String[] {x}));

        var r4str1E = r4str1C.map(TestTypes.R4RecordDeep.class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4str2E = r4str2C.map(TestTypes.R4RecordDeep.class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4str3E = r4str3C.map(TestTypes.R4RecordDeep.class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4str4E = r4str4C.map(TestTypes.R4RecordDeep.class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4str5E = r4str5C.map(TestTypes.R4RecordDeep.class, x -> new TestTypes.R4RecordDeep(List.of(x)));

        var r4str1F = r4str1E.map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));
        var r4str2F = r4str2E.map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));
        var r4str3F = r4str3E.map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));
        var r4str4F = r4str4E.map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));
        var r4str5F = r4str5E.map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));

        var odts1 = str1 .map(OffsetDateTime      .class, h::odt);
        var lds1  = odts1.map(LocalDate           .class, OffsetDateTime::toLocalDate);
        var ldts1 = odts1.map(LocalDateTime       .class, OffsetDateTime::toLocalDateTime);
        var lts1  = odts1.map(LocalTime           .class, OffsetDateTime::toLocalTime);
        var ots1  = odts1.map(OffsetTime          .class, OffsetDateTime::toOffsetTime);
        var ins1  = odts1.map(Instant             .class, OffsetDateTime::toInstant);
        var zdts1 = odts1.map(ZonedDateTime       .class, OffsetDateTime::toZonedDateTime);
        var gcs1  = zdts1.map(GregorianCalendar   .class, GregorianCalendar::from);
        var cs1   = gcs1 .map(Calendar            .class, gc -> gc);
        var uds1  = ins1 .map(java.util.Date      .class, java.util.Date::from);
        var tss1  = ldts1.map(java.sql.Timestamp  .class, java.sql.Timestamp::valueOf);
        var sts1  = lts1 .map(java.sql.Time       .class, java.sql.Time::valueOf);
        var sds1  = lds1 .map(java.sql.Date       .class, java.sql.Date::valueOf);
        var r4dt1 = ldts1.map(TestTypes.R4DateTime.class, TestTypes.R4DateTime::new);

        var odts2 = ldts1.map(OffsetDateTime      .class, x -> x.atOffset(ZoneOffset.UTC));
        var ins2  = odts2.map(Instant             .class, OffsetDateTime::toInstant);
        var zdts2 = odts2.map(ZonedDateTime       .class, OffsetDateTime::toZonedDateTime);
        var gcs2  = zdts2.map(GregorianCalendar   .class, GregorianCalendar::from);
        var cs2   = gcs2 .map(Calendar            .class, gc -> gc);
        var uds2  = ins2 .map(java.util.Date      .class, java.util.Date::from);

        var ldts3 = lds1 .map(LocalDateTime       .class, x -> x.atTime(LocalTime.MIN));
        var odts3 = ldts3.map(OffsetDateTime      .class, x -> x.atOffset(ZoneOffset.UTC));
        var ins3  = odts3.map(Instant             .class, OffsetDateTime::toInstant);
        var zdts3 = odts3.map(ZonedDateTime       .class, OffsetDateTime::toZonedDateTime);
        var gcs3  = zdts3.map(GregorianCalendar   .class, GregorianCalendar::from);
        var cs3   = gcs3 .map(Calendar            .class, gc -> gc);
        var uds3  = ins3 .map(java.util.Date      .class, java.util.Date::from);
        var tss3  = ldts3.map(java.sql.Timestamp  .class, java.sql.Timestamp::valueOf);
        var r4dt3 = ldts3.map(TestTypes.R4DateTime.class, TestTypes.R4DateTime::new);

        var ots5  = lts1 .map(OffsetTime          .class, x -> x.atOffset(ZoneOffset.UTC));

        var all1 = List.of(odts1, lds1, ldts1, lts1, ots1, ins1, zdts1, gcs1, cs1, uds1, tss1, sts1, sds1, str1, r4str1A, r4str1B, r4str1C, r4str1D, r4str1E, r4str1F, r4dt1);
        var all2 = List.of(odts2, lds1, ldts1, lts1, ots5, ins2, zdts2, gcs2, cs2, uds2, tss1, sts1, sds1, str2, r4str2A, r4str2B, r4str2C, r4str2D, r4str2E, r4str2F, r4dt1);
        var all3 = List.of(odts3, lds1, ldts3,             ins3, zdts3, gcs3, cs3, uds3, tss3,       sds1, str3, r4str3A, r4str3B, r4str3C, r4str3D, r4str3E, r4str3F, r4dt3);
        var all4 = List.of(                    lts1, ots1,                                     sts1,       str4, r4str4A, r4str4B, r4str4C, r4str4D, r4str4E, r4str4F);
        var all5 = List.of(                    lts1, ots5,                                     sts1,       str5, r4str5A, r4str5B, r4str5C, r4str5D, r4str5E, r4str5F);

        var cvts  = TestTypes.CVT_CLASSES;
        var cvtsx = TestTypes.CVT_CLASSES_WITH_ARRAYS;

        var odtNode  = h.testIn(prefix1a, cvtsx, odts1, all1);
        var str1Node = h.testIn(prefix1b, cvts , str1 , all1);
        var ldtNode  = h.testIn(prefix2a, cvtsx, ldts1, all2);
        var str2Node = h.testIn(prefix2b, cvts , str2 , all2);
        var ldNode   = h.testIn(prefix3a, cvtsx, lds1 , all3);
        var str3Node = h.testIn(prefix3b, cvts , str3 , all3);
        var otNode   = h.testIn(prefix4a, cvtsx, ots1 , all4);
        var str4Node = h.testIn(prefix4b, cvts , str4 , all4);
        var ltNode   = h.testIn(prefix5a, cvtsx, lts1 , all5);
        var str5Node = h.testIn(prefix5b, cvts , str5 , all5);

        return List.of(odtNode, ldNode, ldtNode, ltNode, otNode, str1Node, str2Node, str3Node, str4Node, str5Node);
    }
}
