import { ArrayModel as ArrayModel_1, ObjectModel as ObjectModel_1 } from "@hilla/form";
import OrderModel_1 from "./OrderModel.js";
import type Sort_1 from "./Sort.js";
declare class SortModel<T extends Sort_1 = Sort_1> extends ObjectModel_1<T> {
    static createEmptyValue: () => Sort_1;
    get orders(): ArrayModel_1<OrderModel_1>;
}
export default SortModel;
//# sourceMappingURL=SortModel.d.ts.map