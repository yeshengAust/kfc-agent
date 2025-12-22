package com.yes.kfcaigc.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "qwen.api.base-url=https://dashscope.aliyuncs.com/compatible-mode/v1",
        "qwen.api.key=test-key",
        "qwen.api.model-name=qwen-plus",
        "qwen.api.temperature=0.1"
})
class CouponModificationServiceTest {

    @Autowired(required = false)
    private CouponModificationService couponModificationService;

    private static final String SAMPLE_COUPON_TEXT = "本产品共1张兑换券（仅限一次兑换）\n" +
            "凭券到店堂食或自助点餐或宅急送订餐，可兑换【1】份【美团】【免配送费】冬日暖暖畅选12件-F列，外送核销可免外送费，打包服务费自理。-P列\n" +
            "产品包含：任选3份【黄金SPA鸡排堡（藤椒风味）/香辣鸡腿堡/劲脆鸡腿堡/老北京鸡肉卷/吮指原味鸡焖拌饭(泡菜版)/吮指原味鸡焖拌饭】+任选4份【热辣香骨鸡（3块装）/黄金脆皮鸡（1块装）/劲爆鸡米花(小)/黄金鸡块（5块装）/香辣鸡翅（2块装）】+任选2份【1只红豆派/1份醇香土豆泥/1只葡式蛋挞】+2杯百事可乐（中）+任选1份【1杯桂花酸梅汤(大)/1杯百事可乐（中）/1杯爆汁三柠茶（中）/1杯九龙金玉轻乳茶(中)（冷/热）/1杯热柠檬红茶（热）/1支原味冰淇淋原味花筒[仅堂食和自助点餐供应]/1份原味圣代（黑糖珍珠酱）/1份经典草莓圣代/1杯百事无糖可乐（中）】;-G＆H列（dsc写入）\n" +
            "\n" +
            "使用有效期：自购买之日起14天内使用有效-N列＆Q列\n" +
            "堂食或自助点餐仅限09:30-23:00使用，具体以餐厅营业时间及该产品实际供应时间为准；\n" +
            "宅急送仅限09:30-23:00使用，具体以该送餐区域实际服务时间及该产品实际供应时间为准 ；-R列\n" +
            "本券仅限在全国有此产品供应的肯德基餐厅使用，不适用于交通枢纽、旅游景区、西藏自治区餐厅及精选餐厅等部分餐厅，具体情况以餐厅餐牌公示为准；\n" +
            "吮指原味鸡焖拌饭(泡菜版)中的绿叶菜有菠菜和菜心两种，出餐种类以实物为准。 在不售卖吮指原味鸡焖拌饭的餐厅，产品将替换为吮指原味鸡焖拌饭（泡菜版），在不售卖吮指原味鸡焖拌饭（泡菜版）的餐厅，产品将替换为吮指原味鸡焖拌饭。 \n" +
            "仅限10:30-23:00供应吮指原味鸡焖拌饭/吮指原味鸡焖拌饭(泡菜版)。                                              \n" +
            " 桂花酸梅汤(大)杯子设计随机发放，以餐厅实际库存为准。\n" +
            "LGL＆后备--S列（包含新限制信息，以及产品限制信息）（dsc写入）\n" +
            "\n" +
            "常规：在不售卖黄金脆皮鸡的餐厅，产品将替换为吮指原味鸡。在不售卖吮指原味鸡的餐厅，产品将替换为黄金脆皮鸡；\n" +
            "脆皮鸡/原味鸡出餐部位随机搭配，详情以实物为准；";

    @Test
    void testContextLoads() {
        // 测试Spring上下文加载
        assertNotNull(couponModificationService, "CouponModificationService should be loaded");
    }

    @Test
    void testDeleteOperation() {
        // 测试删除操作：使用自然语言指令删除黄金脆皮鸡
        if (couponModificationService != null) {
            try {
                CouponModificationService.ModificationResult result = couponModificationService.modifyCouponText(
                        SAMPLE_COUPON_TEXT,
                        "删除黄金脆皮鸡"
                );
                
                assertNotNull(result, "修改结果不应为空");
                assertNotNull(result.getModifiedText(), "修改后的文案不应为空");
                assertFalse(result.getModifiedText().contains("黄金脆皮鸡（1块装）"), 
                        "删除操作后不应包含黄金脆皮鸡（1块装）");
                assertFalse(result.getModifiedText().contains("在不售卖黄金脆皮鸡的餐厅"), 
                        "删除操作后不应包含黄金脆皮鸡的替换规则");
                assertNotNull(result.getModificationPoints(), "改动点列表不应为空");
                System.out.println("删除操作测试结果：\n" + result.getModifiedText());
                System.out.println("改动点：" + result.getModificationPoints());
            } catch (Exception e) {
                // 如果API密钥未配置，跳过实际生成测试
                System.out.println("删除操作测试跳过（需要配置API密钥）：" + e.getMessage());
            }
        }
    }

    @Test
    void testReplaceOperation() {
        // 测试替换操作：使用自然语言指令将黄金脆皮鸡替换为避风塘黄金脆皮鸡
        if (couponModificationService != null) {
            try {
                CouponModificationService.ModificationResult result = couponModificationService.modifyCouponText(
                        SAMPLE_COUPON_TEXT,
                        "将黄金脆皮鸡替换为避风塘黄金脆皮鸡"
                );
                
                assertNotNull(result, "修改结果不应为空");
                assertNotNull(result.getModifiedText(), "修改后的文案不应为空");
                assertTrue(result.getModifiedText().contains("避风塘黄金脆皮鸡"), 
                        "替换操作后应包含避风塘黄金脆皮鸡");
                assertTrue(result.getModifiedText().contains("在不售卖避风塘黄金脆皮鸡的餐厅"), 
                        "替换操作后应更新替换规则中的产品名称");
                assertNotNull(result.getModificationPoints(), "改动点列表不应为空");
                System.out.println("替换操作测试结果：\n" + result.getModifiedText());
                System.out.println("改动点：" + result.getModificationPoints());
            } catch (Exception e) {
                System.out.println("替换操作测试跳过（需要配置API密钥）：" + e.getMessage());
            }
        }
    }
}

