/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kyuubi.spark.connector.tpcds

import scala.collection.JavaConverters._

import io.trino.tpcds.Table
import io.trino.tpcds.Table._
import io.trino.tpcds.column._
import io.trino.tpcds.generator._

object TPCDSSchemaUtils {

  val BASE_TABLES: Array[Table] = Table.getBaseTables.asScala
    .filterNot(_.getName == "dbgen_version").toArray

  // https://github.com/trinodb/tpcds/pull/2
  def reviseColumnName(col: Column, useTableSchema_2_6: Boolean): String = col match {
    case CustomerColumn.C_LAST_REVIEW_DATE_SK if !useTableSchema_2_6 => "c_last_review_date"
    case PromotionColumn.P_RESPONSE_TARGE => "p_response_target"
    case StoreColumn.S_TAX_PRECENTAGE => "s_tax_percentage"
    case right => right.getName
  }

  def tablePartitionColumnNames(table: Table, useTableSchema_2_6: Boolean): Array[String] =
    TABLE_DATE_COLUMNS.getOrElse(table, Array.empty).map(reviseColumnName(_, useTableSchema_2_6))

  private val TABLE_DATE_COLUMNS: Map[Table, Array[Column]] = Map(
    CATALOG_SALES -> Array(CatalogSalesColumn.CS_SOLD_DATE_SK),
    CATALOG_RETURNS -> Array(CatalogReturnsColumn.CR_RETURNED_DATE_SK),
    INVENTORY -> Array(InventoryColumn.INV_DATE_SK),
    STORE_SALES -> Array(StoreSalesColumn.SS_SOLD_DATE_SK),
    STORE_RETURNS -> Array(StoreReturnsColumn.SR_RETURNED_DATE_SK),
    WEB_SALES -> Array(WebSalesColumn.WS_SOLD_DATE_SK),
    WEB_RETURNS -> Array(WebReturnsColumn.WR_RETURNED_DATE_SK))

  def reviseNullColumnIndex(table: Table, index: Int): Int = {
    assert(REVISED_NULL_COLUMN_MAP(table).length == table.getColumns.length)
    REVISED_NULL_COLUMN_MAP(table)(index).getGlobalColumnNumber -
      table.getGeneratorColumns.head.getGlobalColumnNumber
  }

  // Collected from `getValues` method of all Row classes,
  // like: io.trino.tpcds.row.CallCenterRow.getValues
  private val REVISED_NULL_COLUMN_MAP: Map[Table, Array[GeneratorColumn]] = Map(
    TIME_DIM -> Array(
      TimeDimGeneratorColumn.T_TIME_SK,
      TimeDimGeneratorColumn.T_TIME_ID,
      TimeDimGeneratorColumn.T_TIME,
      TimeDimGeneratorColumn.T_HOUR,
      TimeDimGeneratorColumn.T_MINUTE,
      TimeDimGeneratorColumn.T_SECOND,
      TimeDimGeneratorColumn.T_AM_PM,
      TimeDimGeneratorColumn.T_SHIFT,
      TimeDimGeneratorColumn.T_SUB_SHIFT,
      TimeDimGeneratorColumn.T_MEAL_TIME),
    INVENTORY -> Array(
      InventoryGeneratorColumn.INV_DATE_SK,
      InventoryGeneratorColumn.INV_ITEM_SK,
      InventoryGeneratorColumn.INV_WAREHOUSE_SK,
      InventoryGeneratorColumn.INV_QUANTITY_ON_HAND),
    WEB_PAGE -> Array(
      WebPageGeneratorColumn.WP_PAGE_SK,
      WebPageGeneratorColumn.WP_PAGE_ID,
      WebPageGeneratorColumn.WP_REC_START_DATE_ID,
      WebPageGeneratorColumn.WP_REC_END_DATE_ID,
      WebPageGeneratorColumn.WP_CREATION_DATE_SK,
      WebPageGeneratorColumn.WP_ACCESS_DATE_SK,
      WebPageGeneratorColumn.WP_AUTOGEN_FLAG,
      WebPageGeneratorColumn.WP_CUSTOMER_SK,
      WebPageGeneratorColumn.WP_URL,
      WebPageGeneratorColumn.WP_TYPE,
      WebPageGeneratorColumn.WP_CHAR_COUNT,
      WebPageGeneratorColumn.WP_LINK_COUNT,
      WebPageGeneratorColumn.WP_IMAGE_COUNT,
      WebPageGeneratorColumn.WP_MAX_AD_COUNT),
    CUSTOMER_DEMOGRAPHICS -> Array(
      CustomerDemographicsGeneratorColumn.CD_DEMO_SK,
      CustomerDemographicsGeneratorColumn.CD_GENDER,
      CustomerDemographicsGeneratorColumn.CD_MARITAL_STATUS,
      CustomerDemographicsGeneratorColumn.CD_EDUCATION_STATUS,
      CustomerDemographicsGeneratorColumn.CD_PURCHASE_ESTIMATE,
      CustomerDemographicsGeneratorColumn.CD_CREDIT_RATING,
      CustomerDemographicsGeneratorColumn.CD_DEP_COUNT,
      CustomerDemographicsGeneratorColumn.CD_DEP_EMPLOYED_COUNT,
      CustomerDemographicsGeneratorColumn.CD_DEP_COLLEGE_COUNT),
    STORE_RETURNS -> Array(
      StoreReturnsGeneratorColumn.SR_RETURNED_DATE_SK,
      StoreReturnsGeneratorColumn.SR_RETURNED_TIME_SK,
      StoreReturnsGeneratorColumn.SR_ITEM_SK,
      StoreReturnsGeneratorColumn.SR_CUSTOMER_SK,
      StoreReturnsGeneratorColumn.SR_CDEMO_SK,
      StoreReturnsGeneratorColumn.SR_HDEMO_SK,
      StoreReturnsGeneratorColumn.SR_ADDR_SK,
      StoreReturnsGeneratorColumn.SR_STORE_SK,
      StoreReturnsGeneratorColumn.SR_REASON_SK,
      StoreReturnsGeneratorColumn.SR_TICKET_NUMBER,
      StoreReturnsGeneratorColumn.SR_PRICING_QUANTITY,
      StoreReturnsGeneratorColumn.SR_PRICING_NET_PAID,
      StoreReturnsGeneratorColumn.SR_PRICING_EXT_TAX,
      StoreReturnsGeneratorColumn.SR_PRICING_NET_PAID_INC_TAX,
      StoreReturnsGeneratorColumn.SR_PRICING_FEE,
      StoreReturnsGeneratorColumn.SR_PRICING_EXT_SHIP_COST,
      StoreReturnsGeneratorColumn.SR_PRICING_REFUNDED_CASH,
      StoreReturnsGeneratorColumn.SR_PRICING_REVERSED_CHARGE,
      StoreReturnsGeneratorColumn.SR_PRICING_STORE_CREDIT,
      StoreReturnsGeneratorColumn.SR_PRICING_NET_LOSS),
    WEB_SITE -> Array(
      WebSiteGeneratorColumn.WEB_SITE_SK,
      WebSiteGeneratorColumn.WEB_SITE_ID,
      WebSiteGeneratorColumn.WEB_REC_START_DATE_ID,
      WebSiteGeneratorColumn.WEB_REC_END_DATE_ID,
      WebSiteGeneratorColumn.WEB_NAME,
      WebSiteGeneratorColumn.WEB_OPEN_DATE,
      WebSiteGeneratorColumn.WEB_CLOSE_DATE,
      WebSiteGeneratorColumn.WEB_CLASS,
      WebSiteGeneratorColumn.WEB_MANAGER,
      WebSiteGeneratorColumn.WEB_MARKET_ID,
      WebSiteGeneratorColumn.WEB_MARKET_CLASS,
      WebSiteGeneratorColumn.WEB_MARKET_DESC,
      WebSiteGeneratorColumn.WEB_MARKET_MANAGER,
      WebSiteGeneratorColumn.WEB_COMPANY_ID,
      WebSiteGeneratorColumn.WEB_COMPANY_NAME,
      WebSiteGeneratorColumn.WEB_ADDRESS_STREET_NUM,
      WebSiteGeneratorColumn.WEB_ADDRESS_STREET_NAME1,
      WebSiteGeneratorColumn.WEB_ADDRESS_STREET_TYPE,
      WebSiteGeneratorColumn.WEB_ADDRESS_SUITE_NUM,
      WebSiteGeneratorColumn.WEB_ADDRESS_CITY,
      WebSiteGeneratorColumn.WEB_ADDRESS_COUNTY,
      WebSiteGeneratorColumn.WEB_ADDRESS_STATE,
      WebSiteGeneratorColumn.WEB_ADDRESS_ZIP,
      WebSiteGeneratorColumn.WEB_ADDRESS_COUNTRY,
      WebSiteGeneratorColumn.WEB_ADDRESS_GMT_OFFSET,
      WebSiteGeneratorColumn.WEB_TAX_PERCENTAGE),
    CATALOG_SALES -> Array(
      CatalogSalesGeneratorColumn.CS_SOLD_DATE_SK,
      CatalogSalesGeneratorColumn.CS_SOLD_TIME_SK,
      CatalogSalesGeneratorColumn.CS_SHIP_DATE_SK,
      CatalogSalesGeneratorColumn.CS_BILL_CUSTOMER_SK,
      CatalogSalesGeneratorColumn.CS_BILL_CDEMO_SK,
      CatalogSalesGeneratorColumn.CS_BILL_HDEMO_SK,
      CatalogSalesGeneratorColumn.CS_BILL_ADDR_SK,
      CatalogSalesGeneratorColumn.CS_SHIP_CUSTOMER_SK,
      CatalogSalesGeneratorColumn.CS_SHIP_CDEMO_SK,
      CatalogSalesGeneratorColumn.CS_SHIP_HDEMO_SK,
      CatalogSalesGeneratorColumn.CS_SHIP_ADDR_SK,
      CatalogSalesGeneratorColumn.CS_CALL_CENTER_SK,
      CatalogSalesGeneratorColumn.CS_CATALOG_PAGE_SK,
      CatalogSalesGeneratorColumn.CS_SHIP_MODE_SK,
      CatalogSalesGeneratorColumn.CS_WAREHOUSE_SK,
      CatalogSalesGeneratorColumn.CS_SOLD_ITEM_SK,
      CatalogSalesGeneratorColumn.CS_PROMO_SK,
      CatalogSalesGeneratorColumn.CS_ORDER_NUMBER,
      CatalogSalesGeneratorColumn.CS_PRICING_QUANTITY,
      CatalogSalesGeneratorColumn.CS_PRICING_WHOLESALE_COST,
      CatalogSalesGeneratorColumn.CS_PRICING_LIST_PRICE,
      CatalogSalesGeneratorColumn.CS_PRICING_SALES_PRICE,
      CatalogSalesGeneratorColumn.CS_PRICING_EXT_DISCOUNT_AMOUNT,
      CatalogSalesGeneratorColumn.CS_PRICING_EXT_SALES_PRICE,
      CatalogSalesGeneratorColumn.CS_PRICING_EXT_WHOLESALE_COST,
      CatalogSalesGeneratorColumn.CS_PRICING_EXT_LIST_PRICE,
      CatalogSalesGeneratorColumn.CS_PRICING_EXT_TAX,
      CatalogSalesGeneratorColumn.CS_PRICING_COUPON_AMT,
      CatalogSalesGeneratorColumn.CS_PRICING_EXT_SHIP_COST,
      CatalogSalesGeneratorColumn.CS_PRICING_NET_PAID,
      CatalogSalesGeneratorColumn.CS_PRICING_NET_PAID_INC_TAX,
      CatalogSalesGeneratorColumn.CS_PRICING_NET_PAID_INC_SHIP,
      CatalogSalesGeneratorColumn.CS_PRICING_NET_PAID_INC_SHIP_TAX,
      CatalogSalesGeneratorColumn.CS_PRICING_NET_PROFIT),
    SHIP_MODE -> Array(
      ShipModeGeneratorColumn.SM_SHIP_MODE_SK,
      ShipModeGeneratorColumn.SM_SHIP_MODE_ID,
      ShipModeGeneratorColumn.SM_TYPE,
      ShipModeGeneratorColumn.SM_CODE,
      ShipModeGeneratorColumn.SM_CARRIER,
      ShipModeGeneratorColumn.SM_CONTRACT),
    WEB_SALES -> Array(
      WebSalesGeneratorColumn.WS_SOLD_DATE_SK,
      WebSalesGeneratorColumn.WS_SOLD_TIME_SK,
      WebSalesGeneratorColumn.WS_SHIP_DATE_SK,
      WebSalesGeneratorColumn.WS_ITEM_SK,
      WebSalesGeneratorColumn.WS_BILL_CUSTOMER_SK,
      WebSalesGeneratorColumn.WS_BILL_CDEMO_SK,
      WebSalesGeneratorColumn.WS_BILL_HDEMO_SK,
      WebSalesGeneratorColumn.WS_BILL_ADDR_SK,
      WebSalesGeneratorColumn.WS_SHIP_CUSTOMER_SK,
      WebSalesGeneratorColumn.WS_SHIP_CDEMO_SK,
      WebSalesGeneratorColumn.WS_SHIP_HDEMO_SK,
      WebSalesGeneratorColumn.WS_SHIP_ADDR_SK,
      WebSalesGeneratorColumn.WS_WEB_PAGE_SK,
      WebSalesGeneratorColumn.WS_WEB_SITE_SK,
      WebSalesGeneratorColumn.WS_SHIP_MODE_SK,
      WebSalesGeneratorColumn.WS_WAREHOUSE_SK,
      WebSalesGeneratorColumn.WS_PROMO_SK,
      WebSalesGeneratorColumn.WS_ORDER_NUMBER,
      WebSalesGeneratorColumn.WS_PRICING_QUANTITY,
      WebSalesGeneratorColumn.WS_PRICING_WHOLESALE_COST,
      WebSalesGeneratorColumn.WS_PRICING_LIST_PRICE,
      WebSalesGeneratorColumn.WS_PRICING_SALES_PRICE,
      WebSalesGeneratorColumn.WS_PRICING_EXT_DISCOUNT_AMT,
      WebSalesGeneratorColumn.WS_PRICING_EXT_SALES_PRICE,
      WebSalesGeneratorColumn.WS_PRICING_EXT_WHOLESALE_COST,
      WebSalesGeneratorColumn.WS_PRICING_EXT_LIST_PRICE,
      WebSalesGeneratorColumn.WS_PRICING_EXT_TAX,
      WebSalesGeneratorColumn.WS_PRICING_COUPON_AMT,
      WebSalesGeneratorColumn.WS_PRICING_EXT_SHIP_COST,
      WebSalesGeneratorColumn.WS_PRICING_NET_PAID,
      WebSalesGeneratorColumn.WS_PRICING_NET_PAID_INC_TAX,
      WebSalesGeneratorColumn.WS_PRICING_NET_PAID_INC_SHIP,
      WebSalesGeneratorColumn.WS_PRICING_NET_PAID_INC_SHIP_TAX,
      WebSalesGeneratorColumn.WS_PRICING_NET_PROFIT),
    STORE -> Array(
      StoreGeneratorColumn.W_STORE_SK,
      StoreGeneratorColumn.W_STORE_ID,
      StoreGeneratorColumn.W_STORE_REC_START_DATE_ID,
      StoreGeneratorColumn.W_STORE_REC_END_DATE_ID,
      StoreGeneratorColumn.W_STORE_CLOSED_DATE_ID,
      StoreGeneratorColumn.W_STORE_NAME,
      StoreGeneratorColumn.W_STORE_EMPLOYEES,
      StoreGeneratorColumn.W_STORE_FLOOR_SPACE,
      StoreGeneratorColumn.W_STORE_HOURS,
      StoreGeneratorColumn.W_STORE_MANAGER,
      StoreGeneratorColumn.W_STORE_MARKET_ID,
      StoreGeneratorColumn.W_STORE_GEOGRAPHY_CLASS,
      StoreGeneratorColumn.W_STORE_MARKET_DESC,
      StoreGeneratorColumn.W_STORE_MARKET_MANAGER,
      StoreGeneratorColumn.W_STORE_DIVISION_ID,
      StoreGeneratorColumn.W_STORE_DIVISION_NAME,
      StoreGeneratorColumn.W_STORE_COMPANY_ID,
      StoreGeneratorColumn.W_STORE_COMPANY_NAME,
      StoreGeneratorColumn.W_STORE_ADDRESS_STREET_NUM,
      StoreGeneratorColumn.W_STORE_ADDRESS_STREET_NAME1,
      StoreGeneratorColumn.W_STORE_ADDRESS_STREET_TYPE,
      StoreGeneratorColumn.W_STORE_ADDRESS_SUITE_NUM,
      StoreGeneratorColumn.W_STORE_ADDRESS_CITY,
      StoreGeneratorColumn.W_STORE_ADDRESS_COUNTY,
      StoreGeneratorColumn.W_STORE_ADDRESS_STATE,
      StoreGeneratorColumn.W_STORE_ADDRESS_ZIP,
      StoreGeneratorColumn.W_STORE_ADDRESS_COUNTRY,
      StoreGeneratorColumn.W_STORE_ADDRESS_GMT_OFFSET,
      StoreGeneratorColumn.W_STORE_TAX_PERCENTAGE),
    CUSTOMER_ADDRESS -> Array(
      CustomerAddressGeneratorColumn.CA_ADDRESS_SK,
      CustomerAddressGeneratorColumn.CA_ADDRESS_ID,
      CustomerAddressGeneratorColumn.CA_ADDRESS_STREET_NUM,
      CustomerAddressGeneratorColumn.CA_ADDRESS_STREET_NAME,
      CustomerAddressGeneratorColumn.CA_ADDRESS_STREET_TYPE,
      CustomerAddressGeneratorColumn.CA_ADDRESS_SUITE_NUM,
      CustomerAddressGeneratorColumn.CA_ADDRESS_CITY,
      CustomerAddressGeneratorColumn.CA_ADDRESS_COUNTY,
      CustomerAddressGeneratorColumn.CA_ADDRESS_STATE,
      CustomerAddressGeneratorColumn.CA_ADDRESS_ZIP,
      CustomerAddressGeneratorColumn.CA_ADDRESS_COUNTRY,
      CustomerAddressGeneratorColumn.CA_ADDRESS_GMT_OFFSET,
      CustomerAddressGeneratorColumn.CA_LOCATION_TYPE),
    REASON -> Array(
      ReasonGeneratorColumn.R_REASON_SK,
      ReasonGeneratorColumn.R_REASON_ID,
      ReasonGeneratorColumn.R_REASON_DESCRIPTION),
    CATALOG_PAGE -> Array(
      CatalogPageGeneratorColumn.CP_CATALOG_PAGE_SK,
      CatalogPageGeneratorColumn.CP_CATALOG_PAGE_ID,
      CatalogPageGeneratorColumn.CP_START_DATE_ID,
      CatalogPageGeneratorColumn.CP_END_DATE_ID,
      CatalogPageGeneratorColumn.CP_DEPARTMENT,
      CatalogPageGeneratorColumn.CP_CATALOG_NUMBER,
      CatalogPageGeneratorColumn.CP_CATALOG_PAGE_NUMBER,
      CatalogPageGeneratorColumn.CP_DESCRIPTION,
      CatalogPageGeneratorColumn.CP_TYPE),
    PROMOTION -> Array(
      PromotionGeneratorColumn.P_PROMO_SK,
      PromotionGeneratorColumn.P_PROMO_ID,
      PromotionGeneratorColumn.P_START_DATE_ID,
      PromotionGeneratorColumn.P_END_DATE_ID,
      PromotionGeneratorColumn.P_ITEM_SK,
      PromotionGeneratorColumn.P_COST,
      PromotionGeneratorColumn.P_RESPONSE_TARGET,
      PromotionGeneratorColumn.P_PROMO_NAME,
      PromotionGeneratorColumn.P_CHANNEL_DMAIL,
      PromotionGeneratorColumn.P_CHANNEL_EMAIL,
      PromotionGeneratorColumn.P_CHANNEL_CATALOG,
      PromotionGeneratorColumn.P_CHANNEL_TV,
      PromotionGeneratorColumn.P_CHANNEL_RADIO,
      PromotionGeneratorColumn.P_CHANNEL_PRESS,
      PromotionGeneratorColumn.P_CHANNEL_EVENT,
      PromotionGeneratorColumn.P_CHANNEL_DEMO,
      PromotionGeneratorColumn.P_CHANNEL_DETAILS,
      PromotionGeneratorColumn.P_PURPOSE,
      PromotionGeneratorColumn.P_DISCOUNT_ACTIVE),
    CUSTOMER -> Array(
      CustomerGeneratorColumn.C_CUSTOMER_SK,
      CustomerGeneratorColumn.C_CUSTOMER_ID,
      CustomerGeneratorColumn.C_CURRENT_CDEMO_SK,
      CustomerGeneratorColumn.C_CURRENT_HDEMO_SK,
      CustomerGeneratorColumn.C_CURRENT_ADDR_SK,
      CustomerGeneratorColumn.C_FIRST_SHIPTO_DATE_ID,
      CustomerGeneratorColumn.C_FIRST_SALES_DATE_ID,
      CustomerGeneratorColumn.C_SALUTATION,
      CustomerGeneratorColumn.C_FIRST_NAME,
      CustomerGeneratorColumn.C_LAST_NAME,
      CustomerGeneratorColumn.C_PREFERRED_CUST_FLAG,
      CustomerGeneratorColumn.C_BIRTH_DAY,
      CustomerGeneratorColumn.C_BIRTH_MONTH,
      CustomerGeneratorColumn.C_BIRTH_YEAR,
      CustomerGeneratorColumn.C_BIRTH_COUNTRY,
      CustomerGeneratorColumn.C_LOGIN,
      CustomerGeneratorColumn.C_EMAIL_ADDRESS,
      CustomerGeneratorColumn.C_LAST_REVIEW_DATE),
    CATALOG_RETURNS -> Array(
      CatalogReturnsGeneratorColumn.CR_RETURNED_DATE_SK,
      CatalogReturnsGeneratorColumn.CR_RETURNED_TIME_SK,
      CatalogReturnsGeneratorColumn.CR_ITEM_SK,
      CatalogReturnsGeneratorColumn.CR_REFUNDED_CUSTOMER_SK,
      CatalogReturnsGeneratorColumn.CR_REFUNDED_CDEMO_SK,
      CatalogReturnsGeneratorColumn.CR_REFUNDED_HDEMO_SK,
      CatalogReturnsGeneratorColumn.CR_REFUNDED_ADDR_SK,
      CatalogReturnsGeneratorColumn.CR_RETURNING_CUSTOMER_SK,
      CatalogReturnsGeneratorColumn.CR_RETURNING_CDEMO_SK,
      CatalogReturnsGeneratorColumn.CR_RETURNING_HDEMO_SK,
      CatalogReturnsGeneratorColumn.CR_RETURNING_ADDR_SK,
      CatalogReturnsGeneratorColumn.CR_CALL_CENTER_SK,
      CatalogReturnsGeneratorColumn.CR_CATALOG_PAGE_SK,
      CatalogReturnsGeneratorColumn.CR_SHIP_MODE_SK,
      CatalogReturnsGeneratorColumn.CR_WAREHOUSE_SK,
      CatalogReturnsGeneratorColumn.CR_REASON_SK,
      CatalogReturnsGeneratorColumn.CR_ORDER_NUMBER,
      CatalogReturnsGeneratorColumn.CR_PRICING_QUANTITY,
      CatalogReturnsGeneratorColumn.CR_PRICING_NET_PAID,
      CatalogReturnsGeneratorColumn.CR_PRICING_EXT_TAX,
      CatalogReturnsGeneratorColumn.CR_PRICING_NET_PAID_INC_TAX,
      CatalogReturnsGeneratorColumn.CR_PRICING_FEE,
      CatalogReturnsGeneratorColumn.CR_PRICING_EXT_SHIP_COST,
      CatalogReturnsGeneratorColumn.CR_PRICING_REFUNDED_CASH,
      CatalogReturnsGeneratorColumn.CR_PRICING_REVERSED_CHARGE,
      CatalogReturnsGeneratorColumn.CR_PRICING_STORE_CREDIT,
      CatalogReturnsGeneratorColumn.CR_PRICING_NET_LOSS),
    CALL_CENTER -> Array(
      CallCenterGeneratorColumn.CC_CALL_CENTER_SK,
      CallCenterGeneratorColumn.CC_CALL_CENTER_ID,
      CallCenterGeneratorColumn.CC_REC_START_DATE_ID,
      CallCenterGeneratorColumn.CC_REC_END_DATE_ID,
      CallCenterGeneratorColumn.CC_CLOSED_DATE_ID,
      CallCenterGeneratorColumn.CC_OPEN_DATE_ID,
      CallCenterGeneratorColumn.CC_NAME,
      CallCenterGeneratorColumn.CC_CLASS,
      CallCenterGeneratorColumn.CC_EMPLOYEES,
      CallCenterGeneratorColumn.CC_SQ_FT,
      CallCenterGeneratorColumn.CC_HOURS,
      CallCenterGeneratorColumn.CC_MANAGER,
      CallCenterGeneratorColumn.CC_MARKET_ID,
      CallCenterGeneratorColumn.CC_MARKET_CLASS,
      CallCenterGeneratorColumn.CC_MARKET_DESC,
      CallCenterGeneratorColumn.CC_MARKET_MANAGER,
      CallCenterGeneratorColumn.CC_DIVISION,
      CallCenterGeneratorColumn.CC_DIVISION_NAME,
      CallCenterGeneratorColumn.CC_COMPANY,
      CallCenterGeneratorColumn.CC_COMPANY_NAME,
      CallCenterGeneratorColumn.CC_STREET_NUMBER,
      CallCenterGeneratorColumn.CC_STREET_NAME,
      CallCenterGeneratorColumn.CC_STREET_TYPE,
      CallCenterGeneratorColumn.CC_SUITE_NUMBER,
      CallCenterGeneratorColumn.CC_CITY,
      CallCenterGeneratorColumn.CC_ADDRESS,
      CallCenterGeneratorColumn.CC_STATE,
      CallCenterGeneratorColumn.CC_ZIP,
      CallCenterGeneratorColumn.CC_COUNTRY,
      CallCenterGeneratorColumn.CC_GMT_OFFSET,
      CallCenterGeneratorColumn.CC_TAX_PERCENTAGE),
    WEB_RETURNS -> Array(
      WebReturnsGeneratorColumn.WR_RETURNED_DATE_SK,
      WebReturnsGeneratorColumn.WR_RETURNED_TIME_SK,
      WebReturnsGeneratorColumn.WR_ITEM_SK,
      WebReturnsGeneratorColumn.WR_REFUNDED_CUSTOMER_SK,
      WebReturnsGeneratorColumn.WR_REFUNDED_CDEMO_SK,
      WebReturnsGeneratorColumn.WR_REFUNDED_HDEMO_SK,
      WebReturnsGeneratorColumn.WR_REFUNDED_ADDR_SK,
      WebReturnsGeneratorColumn.WR_RETURNING_CUSTOMER_SK,
      WebReturnsGeneratorColumn.WR_RETURNING_CDEMO_SK,
      WebReturnsGeneratorColumn.WR_RETURNING_HDEMO_SK,
      WebReturnsGeneratorColumn.WR_RETURNING_ADDR_SK,
      WebReturnsGeneratorColumn.WR_WEB_PAGE_SK,
      WebReturnsGeneratorColumn.WR_REASON_SK,
      WebReturnsGeneratorColumn.WR_ORDER_NUMBER,
      WebReturnsGeneratorColumn.WR_PRICING_QUANTITY,
      WebReturnsGeneratorColumn.WR_PRICING_NET_PAID,
      WebReturnsGeneratorColumn.WR_PRICING_EXT_TAX,
      WebReturnsGeneratorColumn.WR_PRICING_NET_PAID_INC_TAX,
      WebReturnsGeneratorColumn.WR_PRICING_FEE,
      WebReturnsGeneratorColumn.WR_PRICING_EXT_SHIP_COST,
      WebReturnsGeneratorColumn.WR_PRICING_REFUNDED_CASH,
      WebReturnsGeneratorColumn.WR_PRICING_REVERSED_CHARGE,
      WebReturnsGeneratorColumn.WR_PRICING_STORE_CREDIT,
      WebReturnsGeneratorColumn.WR_PRICING_NET_LOSS),
    STORE_SALES -> Array(
      StoreSalesGeneratorColumn.SS_SOLD_DATE_SK,
      StoreSalesGeneratorColumn.SS_SOLD_TIME_SK,
      StoreSalesGeneratorColumn.SS_SOLD_ITEM_SK,
      StoreSalesGeneratorColumn.SS_SOLD_CUSTOMER_SK,
      StoreSalesGeneratorColumn.SS_SOLD_CDEMO_SK,
      StoreSalesGeneratorColumn.SS_SOLD_HDEMO_SK,
      StoreSalesGeneratorColumn.SS_SOLD_ADDR_SK,
      StoreSalesGeneratorColumn.SS_SOLD_STORE_SK,
      StoreSalesGeneratorColumn.SS_SOLD_PROMO_SK,
      StoreSalesGeneratorColumn.SS_TICKET_NUMBER,
      StoreSalesGeneratorColumn.SS_PRICING_QUANTITY,
      StoreSalesGeneratorColumn.SS_PRICING_WHOLESALE_COST,
      StoreSalesGeneratorColumn.SS_PRICING_LIST_PRICE,
      StoreSalesGeneratorColumn.SS_PRICING_SALES_PRICE,
      StoreSalesGeneratorColumn.SS_PRICING_COUPON_AMT,
      StoreSalesGeneratorColumn.SS_PRICING_EXT_SALES_PRICE,
      StoreSalesGeneratorColumn.SS_PRICING_EXT_WHOLESALE_COST,
      StoreSalesGeneratorColumn.SS_PRICING_EXT_LIST_PRICE,
      StoreSalesGeneratorColumn.SS_PRICING_EXT_TAX,
      StoreSalesGeneratorColumn.SS_PRICING_COUPON_AMT,
      StoreSalesGeneratorColumn.SS_PRICING_NET_PAID,
      StoreSalesGeneratorColumn.SS_PRICING_NET_PAID_INC_TAX,
      StoreSalesGeneratorColumn.SS_PRICING_NET_PROFIT),
    HOUSEHOLD_DEMOGRAPHICS -> Array(
      HouseholdDemographicsGeneratorColumn.HD_DEMO_SK,
      HouseholdDemographicsGeneratorColumn.HD_INCOME_BAND_ID,
      HouseholdDemographicsGeneratorColumn.HD_BUY_POTENTIAL,
      HouseholdDemographicsGeneratorColumn.HD_DEP_COUNT,
      HouseholdDemographicsGeneratorColumn.HD_VEHICLE_COUNT),
    DATE_DIM -> Array(
      DateDimGeneratorColumn.D_DATE_SK,
      DateDimGeneratorColumn.D_DATE_ID,
      DateDimGeneratorColumn.D_DATE_SK,
      DateDimGeneratorColumn.D_MONTH_SEQ,
      DateDimGeneratorColumn.D_WEEK_SEQ,
      DateDimGeneratorColumn.D_QUARTER_SEQ,
      DateDimGeneratorColumn.D_YEAR,
      DateDimGeneratorColumn.D_DOW,
      DateDimGeneratorColumn.D_MOY,
      DateDimGeneratorColumn.D_DOM,
      DateDimGeneratorColumn.D_QOY,
      DateDimGeneratorColumn.D_FY_YEAR,
      DateDimGeneratorColumn.D_FY_QUARTER_SEQ,
      DateDimGeneratorColumn.D_FY_WEEK_SEQ,
      DateDimGeneratorColumn.D_DAY_NAME,
      DateDimGeneratorColumn.D_QUARTER_NAME,
      DateDimGeneratorColumn.D_HOLIDAY,
      DateDimGeneratorColumn.D_WEEKEND,
      DateDimGeneratorColumn.D_FOLLOWING_HOLIDAY,
      DateDimGeneratorColumn.D_FIRST_DOM,
      DateDimGeneratorColumn.D_LAST_DOM,
      DateDimGeneratorColumn.D_SAME_DAY_LY,
      DateDimGeneratorColumn.D_SAME_DAY_LQ,
      DateDimGeneratorColumn.D_CURRENT_DAY,
      DateDimGeneratorColumn.D_CURRENT_WEEK,
      DateDimGeneratorColumn.D_CURRENT_MONTH,
      DateDimGeneratorColumn.D_CURRENT_QUARTER,
      DateDimGeneratorColumn.D_CURRENT_YEAR),
    INCOME_BAND -> Array(
      IncomeBandGeneratorColumn.IB_INCOME_BAND_ID,
      IncomeBandGeneratorColumn.IB_LOWER_BOUND,
      IncomeBandGeneratorColumn.IB_UPPER_BOUND),
    WAREHOUSE -> Array(
      WarehouseGeneratorColumn.W_WAREHOUSE_SK,
      WarehouseGeneratorColumn.W_WAREHOUSE_ID,
      WarehouseGeneratorColumn.W_WAREHOUSE_NAME,
      WarehouseGeneratorColumn.W_WAREHOUSE_SQ_FT,
      WarehouseGeneratorColumn.W_ADDRESS_STREET_NUM,
      WarehouseGeneratorColumn.W_ADDRESS_STREET_NAME1,
      WarehouseGeneratorColumn.W_ADDRESS_STREET_TYPE,
      WarehouseGeneratorColumn.W_ADDRESS_SUITE_NUM,
      WarehouseGeneratorColumn.W_ADDRESS_CITY,
      WarehouseGeneratorColumn.W_ADDRESS_COUNTY,
      WarehouseGeneratorColumn.W_ADDRESS_STATE,
      WarehouseGeneratorColumn.W_ADDRESS_ZIP,
      WarehouseGeneratorColumn.W_ADDRESS_COUNTRY,
      WarehouseGeneratorColumn.W_ADDRESS_GMT_OFFSET),
    ITEM -> Array(
      ItemGeneratorColumn.I_ITEM_SK,
      ItemGeneratorColumn.I_ITEM_ID,
      ItemGeneratorColumn.I_REC_START_DATE_ID,
      ItemGeneratorColumn.I_REC_END_DATE_ID,
      ItemGeneratorColumn.I_ITEM_DESC,
      ItemGeneratorColumn.I_CURRENT_PRICE,
      ItemGeneratorColumn.I_WHOLESALE_COST,
      ItemGeneratorColumn.I_BRAND_ID,
      ItemGeneratorColumn.I_BRAND,
      ItemGeneratorColumn.I_CLASS_ID,
      ItemGeneratorColumn.I_CLASS,
      ItemGeneratorColumn.I_CATEGORY_ID,
      ItemGeneratorColumn.I_CATEGORY,
      ItemGeneratorColumn.I_MANUFACT_ID,
      ItemGeneratorColumn.I_MANUFACT,
      ItemGeneratorColumn.I_SIZE,
      ItemGeneratorColumn.I_FORMULATION,
      ItemGeneratorColumn.I_COLOR,
      ItemGeneratorColumn.I_UNITS,
      ItemGeneratorColumn.I_CONTAINER,
      ItemGeneratorColumn.I_MANAGER_ID,
      ItemGeneratorColumn.I_PRODUCT_NAME))
}
