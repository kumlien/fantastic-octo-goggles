import { NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@hilla/form";
import type Pageable_1 from "./Pageable.js";
import SortModel_1 from "./SortModel.js";
declare class PageableModel<T extends Pageable_1 = Pageable_1> extends ObjectModel_1<T> {
    static createEmptyValue: () => Pageable_1;
    get pageNumber(): NumberModel_1;
    get pageSize(): NumberModel_1;
    get sort(): SortModel_1;
}
export default PageableModel;
//# sourceMappingURL=PageableModel.d.ts.map